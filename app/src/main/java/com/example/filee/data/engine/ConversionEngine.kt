package com.example.filee.data.engine

import android.content.Context
import com.example.filee.data.model.ConversionJob
import com.example.filee.data.model.ConversionOptions
import com.example.filee.data.model.FileFormat
import com.example.filee.data.model.FormatCategory
import com.example.filee.data.model.JobStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ConversionEngine(private val context: Context) {

    suspend fun executeConversion(
        job: ConversionJob,
        onProgressUpdate: (progress: Float, stepText: String) -> Unit
    ): ConversionJob = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            val route = job.route
            var currentBytes = job.rawBytes
                ?: throw IllegalStateException("Input payload is missing or empty")

            val totalHops = (route.hops.size - 1).coerceAtLeast(1)

            for (i in 0 until totalHops) {
                val from = route.hops[i]
                val to = route.hops[i + 1]

                val stepProgressStart = i.toFloat() / totalHops
                val stepProgressEnd = (i + 1).toFloat() / totalHops
                val stepDesc = "Step ${i + 1}/$totalHops: ${from.displayName} ➔ ${to.displayName}"

                onProgressUpdate(stepProgressStart, stepDesc)
                delay(80) // Smooth progress animation feedback

                currentBytes = executeSingleStep(currentBytes, from, to, job.options, job.originalFileName)

                onProgressUpdate(stepProgressEnd, "Completed ${to.displayName}")
            }

            // Save final result to app storage directory
            val outputDir = File(context.filesDir, "converted").apply { mkdirs() }
            val cleanBaseName = job.originalFileName.substringBeforeLast('.', "file")
            val outputFileName = "${cleanBaseName}_converted.${job.targetFormat.extension}"
            val outputFile = File(outputDir, "${System.currentTimeMillis()}_$outputFileName")

            FileOutputStream(outputFile).use { it.write(currentBytes) }

            val duration = System.currentTimeMillis() - startTime
            onProgressUpdate(1.0f, "Finished in ${duration}ms")

            job.copy(
                status = JobStatus.SUCCESS,
                progress = 1.0f,
                currentStepText = "Converted successfully",
                resultFilePath = outputFile.absolutePath,
                resultFileName = outputFileName,
                resultSizeBytes = currentBytes.size.toLong(),
                durationMs = duration
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            job.copy(
                status = JobStatus.FAILED,
                progress = 0f,
                currentStepText = "Error: ${e.message ?: "Conversion failed"}",
                errorMessage = e.message ?: "Unknown error occurred",
                durationMs = duration
            )
        }
    }

    private fun executeSingleStep(
        bytes: ByteArray,
        from: FileFormat,
        to: FileFormat,
        options: ConversionOptions,
        originalName: String
    ): ByteArray {
        // Universal Crypto/Encoding/Archive targets
        if (to == FileFormat.SHA256 || to == FileFormat.MD5 || to == FileFormat.HEX || to == FileFormat.BASE64) {
            return DataConverter.convertData(bytes, from, to, options)
        }
        if (to == FileFormat.ZIP) {
            return ArchiveConverter.createZipSingleFile(bytes, "$originalName.${from.extension}")
        }
        if (to == FileFormat.GZ) {
            return ArchiveConverter.compressGzip(bytes)
        }

        // Image intra-conversions
        if (from.category == FormatCategory.IMAGE && to.category == FormatCategory.IMAGE) {
            return ImageConverter.convertImage(bytes, from, to, options)
        }

        // Image to Document (PDF)
        if (from.category == FormatCategory.IMAGE && to == FileFormat.PDF) {
            return DocumentConverter.convertDocument(context, bytes, from, to, options)
        }

        // PDF to Image or Document
        if (from == FileFormat.PDF) {
            return DocumentConverter.convertDocument(context, bytes, from, to, options)
        }

        // Document intra-conversions (MD, HTML, TXT, RTF, PDF)
        if (from.category == FormatCategory.DOCUMENT && to.category == FormatCategory.DOCUMENT) {
            return DocumentConverter.convertDocument(context, bytes, from, to, options)
        }

        // Text to Audio (TTS)
        if (from == FileFormat.TXT && to == FileFormat.WAV) {
            return AudioConverter.convertAudio(context, bytes, from, to, options)
        }

        // Data conversions (CSV, TSV, JSON, XML, YAML)
        if (from.category == FormatCategory.DATA || to.category == FormatCategory.DATA) {
            return DataConverter.convertData(bytes, from, to, options)
        }

        // Audio conversions
        if (from.category == FormatCategory.AUDIO) {
            return AudioConverter.convertAudio(context, bytes, from, to, options)
        }

        // Archives
        if (from.category == FormatCategory.ARCHIVE) {
            return ArchiveConverter.convertArchive(bytes, from, to, originalName)
        }

        // Fallback text conversion
        return String(bytes, Charsets.UTF_8).toByteArray(Charsets.UTF_8)
    }
}
