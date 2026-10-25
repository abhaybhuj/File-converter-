package com.example.filee.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FileeAmber
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeIndigo
import com.example.ui.theme.FileeRose

enum class FormatCategory(val displayName: String, val color: Color) {
    IMAGE("Images", FileeCyan),
    DOCUMENT("Documents", FileeIndigo),
    DATA("Data & Code", FileeEmerald),
    ARCHIVE("Archives", FileeAmber),
    AUDIO("Audio & Voice", FileeRose),
    CRYPTO("Hashes & Binary", Color(0xFFA855F7))
}

enum class FileFormat(
    val id: String,
    val displayName: String,
    val extension: String,
    val category: FormatCategory,
    val mimeType: String,
    val description: String,
    val supportsCompressionQuality: Boolean = false
) {
    // Images
    PNG("png", "PNG", "png", FormatCategory.IMAGE, "image/png", "Portable Network Graphics (Lossless)"),
    JPEG("jpg", "JPEG", "jpg", FormatCategory.IMAGE, "image/jpeg", "Joint Photographic Experts Group", true),
    WEBP("webp", "WebP", "webp", FormatCategory.IMAGE, "image/webp", "Modern web image format (Lossy & Lossless)", true),
    BMP("bmp", "BMP", "bmp", FormatCategory.IMAGE, "image/bmp", "Standard Windows Bitmap (Uncompressed)"),
    ICO("ico", "ICO", "ico", FormatCategory.IMAGE, "image/x-icon", "Windows Icon format with header table"),
    SVG("svg", "SVG", "svg", FormatCategory.IMAGE, "image/svg+xml", "Scalable Vector Graphics XML"),
    GIF("gif", "GIF", "gif", FormatCategory.IMAGE, "image/gif", "Graphics Interchange Format"),

    // Documents
    PDF("pdf", "PDF", "pdf", FormatCategory.DOCUMENT, "application/pdf", "Adobe Portable Document Format"),
    MD("md", "Markdown", "md", FormatCategory.DOCUMENT, "text/markdown", "Lightweight markdown markup text"),
    HTML("html", "HTML", "html", FormatCategory.DOCUMENT, "text/html", "HyperText Markup Language"),
    TXT("txt", "Plain Text", "txt", FormatCategory.DOCUMENT, "text/plain", "Raw UTF-8 encoded text"),
    RTF("rtf", "RTF", "rtf", FormatCategory.DOCUMENT, "application/rtf", "Rich Text Format document"),

    // Data & Code
    JSON("json", "JSON", "json", FormatCategory.DATA, "application/json", "JavaScript Object Notation data tree"),
    CSV("csv", "CSV", "csv", FormatCategory.DATA, "text/csv", "Comma-Separated Values tabular sheet"),
    TSV("tsv", "TSV", "tsv", FormatCategory.DATA, "text/tab-separated-values", "Tab-Separated Values tabular sheet"),
    XML("xml", "XML", "xml", FormatCategory.DATA, "application/xml", "Extensible Markup Language document"),
    YAML("yaml", "YAML", "yaml", FormatCategory.DATA, "application/x-yaml", "Human-readable data serialization"),

    // Archives
    ZIP("zip", "ZIP", "zip", FormatCategory.ARCHIVE, "application/zip", "ZIP compressed bundle container"),
    GZ("gz", "GZIP", "gz", FormatCategory.ARCHIVE, "application/gzip", "GNU Gzip single stream compression"),

    // Audio
    WAV("wav", "WAV", "wav", FormatCategory.AUDIO, "audio/wav", "Pulse-code modulation uncompressed waveform"),
    MP3("mp3", "MP3", "mp3", FormatCategory.AUDIO, "audio/mpeg", "MPEG Audio Layer III"),
    AAC("aac", "AAC", "aac", FormatCategory.AUDIO, "audio/aac", "Advanced Audio Coding stream"),

    // Hashes & Encodings
    BASE64("b64", "Base64", "txt", FormatCategory.CRYPTO, "text/plain", "RFC 4648 Base64 ASCII representation"),
    HEX("hex", "Hex Dump", "txt", FormatCategory.CRYPTO, "text/plain", "Hexadecimal byte dump with offsets"),
    SHA256("sha256", "SHA-256", "txt", FormatCategory.CRYPTO, "text/plain", "Secure Hash Algorithm 256-bit cryptographic digest"),
    MD5("md5", "MD5", "txt", FormatCategory.CRYPTO, "text/plain", "128-bit cryptographic MD5 checksum");

    companion object {
        fun fromExtension(ext: String?): FileFormat? {
            if (ext == null) return null
            val clean = ext.trim().lowercase().removePrefix(".")
            return entries.firstOrNull { it.extension.equals(clean, ignoreCase = true) || it.id.equals(clean, ignoreCase = true) }
                ?: when (clean) {
                    "jpeg" -> JPEG
                    "htm" -> HTML
                    "markdown" -> MD
                    "text" -> TXT
                    "yml" -> YAML
                    "gzip" -> GZ
                    else -> null
                }
        }

        fun fromFileName(fileName: String): FileFormat? {
            val ext = fileName.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}
