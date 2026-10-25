package com.example.filee.data.engine

import com.example.filee.data.model.FileFormat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ArchiveConverter {

    fun convertArchive(
        inputBytes: ByteArray,
        inputFormat: FileFormat,
        targetFormat: FileFormat,
        fileName: String
    ): ByteArray {
        return when {
            targetFormat == FileFormat.ZIP -> {
                createZipSingleFile(inputBytes, fileName)
            }
            targetFormat == FileFormat.GZ -> {
                compressGzip(inputBytes)
            }
            inputFormat == FileFormat.GZ && targetFormat == FileFormat.TXT -> {
                decompressGzip(inputBytes)
            }
            inputFormat == FileFormat.ZIP && targetFormat == FileFormat.TXT -> {
                inspectZipManifest(inputBytes).toByteArray(Charsets.UTF_8)
            }
            else -> throw UnsupportedOperationException("Archive conversion from ${inputFormat.displayName} to ${targetFormat.displayName} not supported")
        }
    }

    fun createZipSingleFile(bytes: ByteArray, innerFileName: String): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zos ->
            val entry = ZipEntry(innerFileName)
            zos.putNextEntry(entry)
            zos.write(bytes)
            zos.closeEntry()
        }
        return out.toByteArray()
    }

    fun compressGzip(bytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { gzos ->
            gzos.write(bytes)
        }
        return out.toByteArray()
    }

    fun decompressGzip(bytes: ByteArray): ByteArray {
        val inStream = ByteArrayInputStream(bytes)
        val out = ByteArrayOutputStream()
        GZIPInputStream(inStream).use { gzis ->
            gzis.copyTo(out)
        }
        return out.toByteArray()
    }

    fun inspectZipManifest(zipBytes: ByteArray): String {
        val sb = StringBuilder("=== ZIP Archive Manifest ===\n\n")
        var fileCount = 0
        var totalCompressed = 0L
        var totalUncompressed = 0L

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                fileCount++
                val size = if (entry.size >= 0) "${entry.size} bytes" else "Unknown"
                val comp = if (entry.compressedSize >= 0) "${entry.compressedSize} bytes" else "Unknown"
                sb.append("• ${entry.name} [Size: $size | Compressed: $comp]\n")
                if (entry.size > 0) totalUncompressed += entry.size
                if (entry.compressedSize > 0) totalCompressed += entry.compressedSize
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        sb.append("\nSummary: $fileCount entries found.")
        if (totalUncompressed > 0) {
            sb.append("\nTotal Uncompressed: $totalUncompressed bytes")
            if (totalCompressed > 0) {
                val ratio = (100 - (totalCompressed * 100 / totalUncompressed)).coerceAtLeast(0)
                sb.append("\nTotal Compressed: $totalCompressed bytes (Saved $ratio%)")
            }
        }
        return sb.toString()
    }
}
