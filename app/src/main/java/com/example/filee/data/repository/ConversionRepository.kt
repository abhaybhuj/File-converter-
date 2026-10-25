package com.example.filee.data.repository

import com.example.filee.data.engine.ConversionEngine
import com.example.filee.data.local.ConversionRecordDao
import com.example.filee.data.local.ConversionRecordEntity
import com.example.filee.data.model.ConversionJob
import com.example.filee.data.model.JobStatus
import kotlinx.coroutines.flow.Flow

class ConversionRepository(
    private val dao: ConversionRecordDao,
    private val engine: ConversionEngine
) {
    val allRecords: Flow<List<ConversionRecordEntity>> = dao.getAllRecords()
    val favoriteRecords: Flow<List<ConversionRecordEntity>> = dao.getFavoriteRecords()
    val recordCount: Flow<Int> = dao.getRecordCount()
    val totalConvertedBytes: Flow<Long?> = dao.getTotalConvertedBytes()

    suspend fun executeConversion(
        job: ConversionJob,
        onProgressUpdate: (progress: Float, stepText: String) -> Unit
    ): ConversionJob {
        val completedJob = engine.executeConversion(job, onProgressUpdate)

        if (completedJob.status == JobStatus.SUCCESS && completedJob.resultFilePath != null) {
            val record = ConversionRecordEntity(
                sourceFileName = completedJob.originalFileName,
                sourceFormatId = completedJob.inputFormat.id,
                targetFormatId = completedJob.targetFormat.id,
                sourceSizeBytes = completedJob.sourceSizeBytes,
                targetSizeBytes = completedJob.resultSizeBytes,
                durationMs = completedJob.durationMs,
                outputPath = completedJob.resultFilePath,
                outputFileName = completedJob.resultFileName ?: "output.${completedJob.targetFormat.extension}",
                routePath = completedJob.route.formattedPath(),
                isMultiStep = !completedJob.route.isDirect,
                timestamp = System.currentTimeMillis()
            )
            dao.insertRecord(record)
        }

        return completedJob
    }

    suspend fun toggleFavorite(record: ConversionRecordEntity) {
        dao.updateRecord(record.copy(isFavorite = !record.isFavorite))
    }

    suspend fun deleteRecord(record: ConversionRecordEntity) {
        dao.deleteRecord(record)
    }

    suspend fun clearAllHistory() {
        dao.clearAll()
    }
}
