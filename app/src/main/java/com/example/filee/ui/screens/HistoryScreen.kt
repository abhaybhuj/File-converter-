package com.example.filee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.local.ConversionRecordEntity
import com.example.filee.data.model.FileFormat
import com.example.ui.theme.FileeAmber
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import com.example.filee.ui.viewmodel.FileeViewModel
import com.example.filee.util.FileUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: FileeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyRecords by viewModel.historyRecords.collectAsState()
    val totalCount by viewModel.totalRecordsCount.collectAsState()
    val totalBytes by viewModel.totalConvertedBytes.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_history")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Conversion History",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "$totalCount items persisted offline in Room DB",
                    style = MaterialTheme.typography.labelSmall,
                    color = FileeCyan
                )
            }

            if (historyRecords.isNotEmpty()) {
                FilledTonalButton(
                    onClick = { viewModel.clearHistory() },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = FileeNavySurfaceVariant),
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metrics Summary Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FileeNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(title = "Total Converted", value = "$totalCount")
                MetricColumn(title = "Data Processed", value = FileUtils.formatFileSize(totalBytes ?: 0L))
                MetricColumn(title = "Privacy Mode", value = "100% Offline")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Conversions Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Completed conversions are automatically cataloged here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyRecords, key = { it.id }) { record ->
                    HistoryRecordCard(
                        record = record,
                        onShare = {
                            val format = FileFormat.entries.firstOrNull { it.id == record.targetFormatId } ?: FileFormat.TXT
                            FileUtils.shareFile(context, record.outputPath, format)
                        },
                        onOpen = {
                            val format = FileFormat.entries.firstOrNull { it.id == record.targetFormatId } ?: FileFormat.TXT
                            FileUtils.openFile(context, record.outputPath, format)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(record) },
                        onDelete = { viewModel.deleteHistoryRecord(record) }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = FileeCyan
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun HistoryRecordCard(
    record: ConversionRecordEntity,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(record.timestamp))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = FileeNavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
        modifier = Modifier.fillMaxWidth().testTag("history_record_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FileeCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = FileeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.outputFileName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${record.routePath} • $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (record.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (record.isFavorite) FileeAmber else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FileeNavySurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${FileUtils.formatFileSize(record.sourceSizeBytes)} ➔ ${FileUtils.formatFileSize(record.targetSizeBytes)} (${record.durationMs}ms)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCBD5E1)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onOpen, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Open",
                            tint = FileeCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = FileeEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
