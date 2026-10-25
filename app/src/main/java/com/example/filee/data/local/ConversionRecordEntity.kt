package com.example.filee.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_records")
data class ConversionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceFileName: String,
    val sourceFormatId: String,
    val targetFormatId: String,
    val sourceSizeBytes: Long,
    val targetSizeBytes: Long,
    val durationMs: Long,
    val outputPath: String,
    val outputFileName: String,
    val routePath: String,
    val isMultiStep: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
