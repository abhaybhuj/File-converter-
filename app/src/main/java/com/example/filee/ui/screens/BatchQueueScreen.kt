package com.example.filee.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.model.FileFormat
import com.example.filee.data.model.JobStatus
import com.example.filee.ui.components.ConversionItemCard
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import com.example.filee.ui.viewmodel.FileeViewModel
import com.example.filee.ui.viewmodel.SelectedFile
import com.example.filee.util.FileUtils
import com.example.filee.util.SampleFilesUtil

@Composable
fun BatchQueueScreen(
    viewModel: FileeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val batchQueue by viewModel.batchQueue.collectAsState()
    val isBatchConverting by viewModel.isBatchConverting.collectAsState()

    // File picker using ActivityResultContracts.GetMultipleContents for batch processing
    val multiFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val selectedFiles = uris.mapNotNull { uri ->
            val name = FileUtils.getFileNameFromUri(context, uri)
            val bytes = FileUtils.readBytesFromUri(context, uri)
            if (bytes != null) {
                val format = FileFormat.fromFileName(name) ?: FileFormat.TXT
                SelectedFile(name, format, bytes, uri)
            } else null
        }
        if (selectedFiles.isNotEmpty()) {
            viewModel.addFilesToBatch(selectedFiles)
        }
    }

    val completedCount = batchQueue.count { it.status == JobStatus.SUCCESS }
    val totalCount = batchQueue.size
    val batchProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_batch_queue")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Batch Queue",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "$completedCount of $totalCount converted",
                    style = MaterialTheme.typography.labelSmall,
                    color = FileeCyan
                )
            }

            if (batchQueue.isNotEmpty()) {
                FilledTonalButton(
                    onClick = { viewModel.clearBatch() },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = FileeNavySurfaceVariant),
                    modifier = Modifier.testTag("btn_clear_batch")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Bar: Add Files & Add Sample Pack
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { multiFilePicker.launch("*/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_add_files"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = FileeCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Files", fontSize = 12.sp, color = Color.White)
            }

            FilledTonalButton(
                onClick = {
                    val samples = SampleFilesUtil.getSampleFiles().map {
                        SelectedFile(it.name, it.format, it.bytes)
                    }
                    viewModel.addFilesToBatch(samples)
                },
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .testTag("btn_load_sample_pack"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FileeNavySurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = FileeEmerald
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Load Sample Pack", fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress indicator
        if (totalCount > 0) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = FileeNavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBatchConverting) "Batch Processing Active..." else "Queue Ready",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isBatchConverting) FileeCyan else Color.White
                        )
                        Text(
                            text = "${(batchProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = FileeCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { batchProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FileeCyan,
                        trackColor = FileeNavySurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Batch Queue List
        if (batchQueue.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Batch Queue is Empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Tap 'Load Sample Pack' or 'Add Files' to queue bulk offline conversions.",
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
                items(batchQueue, key = { it.id }) { job ->
                    ConversionItemCard(
                        job = job,
                        onDownload = {
                            if (job.resultFilePath != null) {
                                FileUtils.saveToDownloads(
                                    context = context,
                                    filePath = job.resultFilePath,
                                    targetFileName = job.resultFileName ?: "converted.${job.targetFormat.extension}",
                                    format = job.targetFormat
                                )
                            }
                        },
                        onShare = {
                            if (job.resultFilePath != null) {
                                FileUtils.shareFile(context, job.resultFilePath, job.targetFormat)
                            }
                        },
                        onOpen = {
                            if (job.resultFilePath != null) {
                                FileUtils.openFile(context, job.resultFilePath, job.targetFormat)
                            }
                        },
                        onRemove = { viewModel.removeBatchJob(job.id) },
                        onRetry = { /* Retry */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Start Batch Button
            Button(
                onClick = { viewModel.startBatchConversion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_start_batch"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FileeCyan,
                    contentColor = Color(0xFF0F172A)
                ),
                enabled = !isBatchConverting
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBatchConverting) "Converting Batch..." else "Convert All Files",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
