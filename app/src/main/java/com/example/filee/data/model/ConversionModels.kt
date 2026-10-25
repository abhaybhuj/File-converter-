package com.example.filee.data.model

import android.net.Uri

data class ConversionRoute(
    val inputFormat: FileFormat,
    val outputFormat: FileFormat,
    val hops: List<FileFormat>,
    val stepDescriptions: List<String>,
    val estimatedFidelity: String
) {
    val isDirect: Boolean get() = hops.size == 2
    val hopCount: Int get() = (hops.size - 1).coerceAtLeast(1)

    fun formattedPath(): String = hops.joinToString(separator = " ➔ ") { it.displayName }
}

data class ConversionOptions(
    val imageQuality: Int = 85,
    val resizePercentage: Int = 100,
    val pdfOrientationLandscape: Boolean = false,
    val pdfMarginDp: Int = 24,
    val csvDelimiter: Char = ',',
    val prettyPrintJson: Boolean = true,
    val ttsSpeed: Float = 1.0f
)

enum class JobStatus {
    PENDING,
    ROUTING,
    CONVERTING,
    SUCCESS,
    FAILED,
    CANCELLED
}

data class ConversionJob(
    val id: String,
    val originalFileName: String,
    val sourceUri: Uri? = null,
    val rawBytes: ByteArray? = null,
    val sourceSizeBytes: Long = 0L,
    val inputFormat: FileFormat,
    val targetFormat: FileFormat,
    val route: ConversionRoute,
    val options: ConversionOptions = ConversionOptions(),
    val status: JobStatus = JobStatus.PENDING,
    val progress: Float = 0f,
    val currentStepText: String = "Ready",
    val resultFilePath: String? = null,
    val resultFileName: String? = null,
    val resultSizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val sizeDeltaPercent: Int
        get() = if (sourceSizeBytes > 0 && resultSizeBytes > 0) {
            val delta = ((resultSizeBytes - sourceSizeBytes).toDouble() / sourceSizeBytes.toDouble()) * 100
            delta.toInt()
        } else 0
}
