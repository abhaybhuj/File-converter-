package com.example.filee.data.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.example.filee.data.model.ConversionOptions
import com.example.filee.data.model.FileFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object DocumentConverter {

    fun convertDocument(
        context: Context,
        inputBytes: ByteArray,
        inputFormat: FileFormat,
        targetFormat: FileFormat,
        options: ConversionOptions
    ): ByteArray {
        val inputString = if (inputFormat != FileFormat.PDF) {
            String(inputBytes, Charsets.UTF_8)
        } else ""

        return when {
            // PDF to image rasterization
            inputFormat == FileFormat.PDF && (targetFormat == FileFormat.PNG || targetFormat == FileFormat.JPEG || targetFormat == FileFormat.WEBP) -> {
                renderPdfToImage(context, inputBytes, targetFormat, options)
            }
            // PDF to text extraction
            inputFormat == FileFormat.PDF && targetFormat == FileFormat.TXT -> {
                extractTextFromPdfHeuristic(inputBytes)
            }
            // Markdown conversions
            inputFormat == FileFormat.MD && targetFormat == FileFormat.HTML -> {
                convertMarkdownToHtml(inputString).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.MD && targetFormat == FileFormat.TXT -> {
                stripMarkdownToPlainText(inputString).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.MD && targetFormat == FileFormat.PDF -> {
                createPdfFromMarkdown(inputString, options)
            }
            inputFormat == FileFormat.MD && targetFormat == FileFormat.RTF -> {
                convertMarkdownToRtf(inputString).toByteArray(Charsets.UTF_8)
            }
            // HTML conversions
            inputFormat == FileFormat.HTML && targetFormat == FileFormat.TXT -> {
                stripHtmlToPlainText(inputString).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.HTML && targetFormat == FileFormat.MD -> {
                convertHtmlToMarkdown(inputString).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.HTML && targetFormat == FileFormat.PDF -> {
                val plainText = stripHtmlToPlainText(inputString)
                createPdfFromPlainText(plainText, "HTML Document", options)
            }
            // Text to PDF
            inputFormat == FileFormat.TXT && targetFormat == FileFormat.PDF -> {
                createPdfFromPlainText(inputString, "Document", options)
            }
            inputFormat == FileFormat.TXT && targetFormat == FileFormat.HTML -> {
                convertPlainTextToHtml(inputString).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.TXT && targetFormat == FileFormat.MD -> {
                inputBytes // Plain text is already valid Markdown
            }
            // RTF to Text/HTML/PDF
            inputFormat == FileFormat.RTF && targetFormat == FileFormat.TXT -> {
                stripRtfToPlainText(inputString).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.RTF && targetFormat == FileFormat.HTML -> {
                convertPlainTextToHtml(stripRtfToPlainText(inputString)).toByteArray(Charsets.UTF_8)
            }
            inputFormat == FileFormat.RTF && targetFormat == FileFormat.PDF -> {
                createPdfFromPlainText(stripRtfToPlainText(inputString), "Rich Text Document", options)
            }
            // Image to PDF
            inputFormat.category == com.example.filee.data.model.FormatCategory.IMAGE && targetFormat == FileFormat.PDF -> {
                val bitmap = ImageConverter.decodeBitmap(inputBytes)
                    ?: throw IllegalArgumentException("Failed to decode image for PDF conversion")
                createPdfFromBitmap(bitmap, options)
            }
            else -> throw UnsupportedOperationException("Document conversion from ${inputFormat.displayName} to ${targetFormat.displayName} is not directly supported.")
        }
    }

    private fun renderPdfToImage(
        context: Context,
        pdfBytes: ByteArray,
        targetFormat: FileFormat,
        options: ConversionOptions
    ): ByteArray {
        val tempFile = File(context.cacheDir, "temp_render_${System.currentTimeMillis()}.pdf")
        try {
            FileOutputStream(tempFile).use { it.write(pdfBytes) }
            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (renderer.pageCount == 0) throw IllegalStateException("PDF has 0 pages")
                    renderer.openPage(0).use { page ->
                        val density = 2 // 2x scale for sharp output
                        val width = page.width * density
                        val height = page.height * density
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        return ImageConverter.convertImage(
                            inputBytes = ImageConverter.convertImage(
                                inputBytes = ByteArray(0),
                                inputFormat = FileFormat.PNG,
                                targetFormat = targetFormat,
                                options = options
                            ),
                            inputFormat = FileFormat.PNG,
                            targetFormat = targetFormat,
                            options = options
                        ).let {
                            val out = ByteArrayOutputStream()
                            when (targetFormat) {
                                FileFormat.JPEG -> bitmap.compress(Bitmap.CompressFormat.JPEG, options.imageQuality, out)
                                FileFormat.WEBP -> bitmap.compress(Bitmap.CompressFormat.WEBP, options.imageQuality, out)
                                else -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            out.toByteArray()
                        }
                    }
                }
            }
        } finally {
            tempFile.delete()
        }
    }

    private fun extractTextFromPdfHeuristic(pdfBytes: ByteArray): ByteArray {
        val raw = String(pdfBytes, Charsets.ISO_8859_1)
        val sb = StringBuilder()
        val regex = Regex("""\((.*?)\)\s*Tj""")
        val matches = regex.findAll(raw)
        for (m in matches) {
            sb.append(m.groupValues[1]).append(" ")
        }
        val result = if (sb.isNotEmpty()) sb.toString().trim() else "PDF Document [Binary Content: ${pdfBytes.size} bytes]"
        return result.toByteArray(Charsets.UTF_8)
    }

    fun createPdfFromPlainText(text: String, title: String, options: ConversionOptions): ByteArray {
        val doc = PdfDocument()
        val pageW = if (options.pdfOrientationLandscape) 842 else 595 // Standard A4 points
        val pageH = if (options.pdfOrientationLandscape) 595 else 842
        val margin = options.pdfMarginDp * 2 // Points

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val lines = text.split("\n")
        val lineHeight = 18f
        val usableHeight = pageH - margin * 2 - 50 // Leave room for title & footer

        var currentLineIndex = 0
        var pageNumber = 1

        while (currentLineIndex < lines.size || pageNumber == 1) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            var y = margin + 24f

            // Title on first page
            if (pageNumber == 1) {
                canvas.drawText(title, margin.toFloat(), y, titlePaint)
                y += 32f
            }

            // Draw lines on page
            while (currentLineIndex < lines.size && y + lineHeight < pageH - margin) {
                val line = lines[currentLineIndex]
                // Wrap text if needed
                val maxChars = ((pageW - margin * 2) / 7).toInt().coerceAtLeast(20)
                if (line.length > maxChars) {
                    val chunks = line.chunked(maxChars)
                    for (c in chunks) {
                        canvas.drawText(c, margin.toFloat(), y, paint)
                        y += lineHeight
                    }
                } else {
                    canvas.drawText(line, margin.toFloat(), y, paint)
                    y += lineHeight
                }
                currentLineIndex++
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("Page $pageNumber • Converted by Filee", margin.toFloat(), (pageH - margin / 2).toFloat(), footerPaint)

            doc.finishPage(page)
            pageNumber++
        }

        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    fun createPdfFromMarkdown(markdown: String, options: ConversionOptions): ByteArray {
        val cleanText = stripMarkdownToPlainText(markdown)
        return createPdfFromPlainText(cleanText, "Markdown Document", options)
    }

    fun createPdfFromBitmap(bitmap: Bitmap, options: ConversionOptions): ByteArray {
        val doc = PdfDocument()
        val pageW = if (options.pdfOrientationLandscape) 842 else 595
        val pageH = if (options.pdfOrientationLandscape) 595 else 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val margin = options.pdfMarginDp * 2f
        val availW = pageW - margin * 2
        val availH = pageH - margin * 2

        val scale = minOf(availW / bitmap.width.toFloat(), availH / bitmap.height.toFloat())
        val drawW = (bitmap.width * scale).toInt()
        val drawH = (bitmap.height * scale).toInt()
        val left = margin + (availW - drawW) / 2f
        val top = margin + (availH - drawH) / 2f

        val scaled = Bitmap.createScaledBitmap(bitmap, drawW, drawH, true)
        canvas.drawBitmap(scaled, left, top, null)

        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    fun convertMarkdownToHtml(markdown: String): String {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Filee Document</title>")
        sb.append("<style>body{font-family:system-ui,sans-serif;max-width:800px;margin:32px auto;padding:0 16px;line-height:1.6;color:#1e293b}h1,h2,h3{color:#0f172a}pre{background:#f1f5f9;padding:12px;border-radius:6px;overflow-x:auto}code{background:#f1f5f9;padding:2px 6px;border-radius:4px}table{border-collapse:collapse;width:100%}th,td{border:1px solid #cbd5e1;padding:8px;text-align:left}th{background:#f8fafc}</style></head><body>\n")

        val lines = markdown.split("\n")
        var inList = false
        var inCode = false

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("```") -> {
                    if (inCode) {
                        sb.append("</code></pre>\n")
                        inCode = false
                    } else {
                        sb.append("<pre><code>")
                        inCode = true
                    }
                }
                inCode -> {
                    sb.append(escapeHtml(line)).append("\n")
                }
                trimmed.startsWith("# ") -> sb.append("<h1>").append(escapeHtml(trimmed.substring(2))).append("</h1>\n")
                trimmed.startsWith("## ") -> sb.append("<h2>").append(escapeHtml(trimmed.substring(3))).append("</h2>\n")
                trimmed.startsWith("### ") -> sb.append("<h3>").append(escapeHtml(trimmed.substring(4))).append("</h3>\n")
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    if (!inList) {
                        sb.append("<ul>\n")
                        inList = true
                    }
                    sb.append("<li>").append(escapeHtml(trimmed.substring(2))).append("</li>\n")
                }
                trimmed.isEmpty() -> {
                    if (inList) {
                        sb.append("</ul>\n")
                        inList = false
                    }
                    sb.append("<br/>\n")
                }
                else -> {
                    if (inList) {
                        sb.append("</ul>\n")
                        inList = false
                    }
                    sb.append("<p>").append(escapeHtml(trimmed)).append("</p>\n")
                }
            }
        }
        if (inList) sb.append("</ul>\n")
        if (inCode) sb.append("</code></pre>\n")

        sb.append("</body></html>")
        return sb.toString()
    }

    fun stripMarkdownToPlainText(markdown: String): String {
        return markdown
            .replace(Regex("""^#{1,6}\s+""", RegexOption.MULTILINE), "")
            .replace(Regex("""\*\*(.*?)\*\*"""), "$1")
            .replace(Regex("""\*(.*?)\*"""), "$1")
            .replace(Regex("""`{1,3}(.*?)`{1,3}"""), "$1")
            .replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1")
            .replace(Regex("""^[-*+]\s+""", RegexOption.MULTILINE), "• ")
            .trim()
    }

    fun stripHtmlToPlainText(html: String): String {
        return html
            .replace(Regex("""<style[\s\S]*?</style>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<script[\s\S]*?</script>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""</p>""", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("""</li>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""<[^>]*>"""), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .trim()
    }

    fun convertHtmlToMarkdown(html: String): String {
        var md = html
            .replace(Regex("""<h1>(.*?)</h1>""", RegexOption.IGNORE_CASE), "# $1\n\n")
            .replace(Regex("""<h2>(.*?)</h2>""", RegexOption.IGNORE_CASE), "## $1\n\n")
            .replace(Regex("""<h3>(.*?)</h3>""", RegexOption.IGNORE_CASE), "### $1\n\n")
            .replace(Regex("""<b>(.*?)</b>""", RegexOption.IGNORE_CASE), "**$1**")
            .replace(Regex("""<strong>(.*?)</strong>""", RegexOption.IGNORE_CASE), "**$1**")
            .replace(Regex("""<i>(.*?)</i>""", RegexOption.IGNORE_CASE), "*$1*")
            .replace(Regex("""<em>(.*?)</em>""", RegexOption.IGNORE_CASE), "*$1*")
            .replace(Regex("""<li>(.*?)</li>""", RegexOption.IGNORE_CASE), "- $1\n")
            .replace(Regex("""<a href="(.*?)">(.*?)</a>""", RegexOption.IGNORE_CASE), "[$2]($1)")

        return stripHtmlToPlainText(md)
    }

    fun convertPlainTextToHtml(text: String): String {
        val lines = text.split("\n")
        val sb = StringBuilder("<!DOCTYPE html><html><body>\n")
        for (l in lines) {
            sb.append("<p>").append(escapeHtml(l)).append("</p>\n")
        }
        sb.append("</body></html>")
        return sb.toString()
    }

    fun convertMarkdownToRtf(markdown: String): String {
        val plain = stripMarkdownToPlainText(markdown)
        return "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Arial;}}\\f0\\fs24 " +
                plain.replace("\n", "\\par ") +
                "}"
    }

    fun stripRtfToPlainText(rtf: String): String {
        return rtf
            .replace(Regex("""\{\\\*?\w+(\s+[^;\}]+)?;?\}"""), "")
            .replace(Regex("""\\[a-zA-Z]+(-?\d+)?"""), "")
            .replace(Regex("""[\{\}]"""), "")
            .trim()
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
