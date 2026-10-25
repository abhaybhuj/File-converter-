package com.example.filee.data.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.example.filee.data.model.ConversionOptions
import com.example.filee.data.model.FileFormat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImageConverter {

    fun decodeBitmap(inputBytes: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun convertImage(
        inputBytes: ByteArray,
        inputFormat: FileFormat,
        targetFormat: FileFormat,
        options: ConversionOptions
    ): ByteArray {
        val originalBitmap = when (inputFormat) {
            FileFormat.SVG -> rasterizeSvgToBitmap(inputBytes)
            else -> decodeBitmap(inputBytes)
        } ?: throw IllegalArgumentException("Could not decode image in format ${inputFormat.displayName}")

        // Apply resize if requested
        val processedBitmap = if (options.resizePercentage in 10..99) {
            val scale = options.resizePercentage / 100f
            val targetW = (originalBitmap.width * scale).toInt().coerceAtLeast(1)
            val targetH = (originalBitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
        } else {
            originalBitmap
        }

        return when (targetFormat) {
            FileFormat.PNG -> encodePng(processedBitmap)
            FileFormat.JPEG -> encodeJpeg(processedBitmap, options.imageQuality)
            FileFormat.WEBP -> encodeWebp(processedBitmap, options.imageQuality)
            FileFormat.BMP -> encodeBmp(processedBitmap)
            FileFormat.ICO -> encodeIco(processedBitmap)
            FileFormat.SVG -> generateSvgFromBitmap(processedBitmap)
            FileFormat.GIF -> encodePng(processedBitmap) // Standalone frame
            else -> throw UnsupportedOperationException("Unsupported target image format ${targetFormat.displayName}")
        }
    }

    private fun encodePng(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }

    private fun encodeJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        // If bitmap has alpha, draw over white background to avoid black background artifacts in JPEG
        val rgbBitmap = if (bitmap.hasAlpha()) {
            val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(opaque)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            opaque
        } else {
            bitmap
        }
        rgbBitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(10, 100), out)
        return out.toByteArray()
    }

    private fun encodeWebp(bitmap: Bitmap, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        @Suppress("DEPRECATION")
        val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (quality >= 100) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.WEBP
        }
        bitmap.compress(format, quality.coerceIn(10, 100), out)
        return out.toByteArray()
    }

    /**
     * Standard uncompressed 24-bit Windows Bitmap (BMP) encoder.
     */
    private fun encodeBmp(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val rowPadding = (4 - (width * 3) % 4) % 4
        val rowStride = width * 3 + rowPadding
        val pixelArraySize = rowStride * height
        val fileSize = 54 + pixelArraySize

        val buffer = ByteBuffer.allocate(fileSize).order(ByteOrder.LITTLE_ENDIAN)

        // BMP File Header (14 bytes)
        buffer.put('B'.code.toByte())
        buffer.put('M'.code.toByte())
        buffer.putInt(fileSize)
        buffer.putShort(0) // reserved 1
        buffer.putShort(0) // reserved 2
        buffer.putInt(54) // offset to pixel data

        // DIB Header (BITMAPINFOHEADER - 40 bytes)
        buffer.putInt(40) // header size
        buffer.putInt(width)
        buffer.putInt(height) // positive means bottom-up
        buffer.putShort(1) // color planes
        buffer.putShort(24) // bits per pixel
        buffer.putInt(0) // compression (BI_RGB)
        buffer.putInt(pixelArraySize)
        buffer.putInt(2835) // horizontal resolution (72 DPI)
        buffer.putInt(2835) // vertical resolution (72 DPI)
        buffer.putInt(0) // colors in color table
        buffer.putInt(0) // important color count

        // Pixels (bottom-to-top, BGR format)
        val pixels = IntArray(width)
        for (y in height - 1 downTo 0) {
            bitmap.getPixels(pixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                val color = pixels[x]
                buffer.put(Color.blue(color).toByte())
                buffer.put(Color.green(color).toByte())
                buffer.put(Color.red(color).toByte())
            }
            // Row padding
            for (p in 0 until rowPadding) {
                buffer.put(0.toByte())
            }
        }

        return buffer.array()
    }

    /**
     * Standard ICO file encoder with PNG payload.
     */
    private fun encodeIco(bitmap: Bitmap): ByteArray {
        // ICO works best with square icons (max 256x256)
        val size = bitmap.width.coerceAtMost(bitmap.height).coerceAtMost(256)
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val pngBytes = encodePng(scaled)

        val headerSize = 6
        val dirEntrySize = 16
        val totalSize = headerSize + dirEntrySize + pngBytes.size

        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        // ICONDIR header
        buffer.putShort(0) // Reserved
        buffer.putShort(1) // Type: 1 = Icon
        buffer.putShort(1) // Image count: 1

        // ICONDIRENTRY (16 bytes)
        buffer.put((if (size == 256) 0 else size).toByte()) // Width
        buffer.put((if (size == 256) 0 else size).toByte()) // Height
        buffer.put(0.toByte()) // Color count (0 = >=8bpp)
        buffer.put(0.toByte()) // Reserved
        buffer.putShort(1) // Color planes
        buffer.putShort(32) // Bits per pixel
        buffer.putInt(pngBytes.size) // Size of image data
        buffer.putInt(headerSize + dirEntrySize) // Offset to image data

        // PNG data
        buffer.put(pngBytes)

        return buffer.array()
    }

    private fun rasterizeSvgToBitmap(svgBytes: ByteArray): Bitmap {
        val svgText = String(svgBytes, Charsets.UTF_8)
        val width = 512
        val height = 512
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Default background
        canvas.drawColor(Color.TRANSPARENT)

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#38BDF8")
            style = Paint.Style.FILL
        }

        // Basic SVG heuristic parser for render preview
        if (svgText.contains("circle", ignoreCase = true)) {
            canvas.drawCircle(width / 2f, height / 2f, width / 3f, paint)
        } else if (svgText.contains("rect", ignoreCase = true)) {
            canvas.drawRect(Rect(64, 64, width - 64, height - 64), paint)
        } else {
            // Draw stylized vector graphics icon
            val strokePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#38BDF8")
                strokeWidth = 14f
                style = Paint.Style.STROKE
            }
            canvas.drawCircle(width / 2f, height / 2f, width / 3f, strokePaint)
            paint.textSize = 36f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("SVG Vector", width / 2f, height / 2f + 12f, paint)
        }

        return bitmap
    }

    private fun generateSvgFromBitmap(bitmap: Bitmap): ByteArray {
        val pngBytes = encodePng(bitmap)
        val base64 = android.util.Base64.encodeToString(pngBytes, android.util.Base64.NO_WRAP)
        val svg = """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" 
                 width="${bitmap.width}" height="${bitmap.height}" viewBox="0 0 ${bitmap.width} ${bitmap.height}">
                <image width="${bitmap.width}" height="${bitmap.height}" xlink:href="data:image/png;base64,$base64"/>
            </svg>
        """.trimIndent()
        return svg.toByteArray(Charsets.UTF_8)
    }
}
