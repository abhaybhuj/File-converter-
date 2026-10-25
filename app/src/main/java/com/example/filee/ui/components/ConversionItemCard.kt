package com.example.filee.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.model.ConversionJob
import com.example.filee.data.model.JobStatus
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import com.example.ui.theme.FileeRose
import com.example.filee.util.FileUtils

@Composable
fun ConversionItemCard(
    job: ConversionJob,
    onDownload: () -> Unit = {},
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        color = FileeNavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: File Icon, Name, Format Pill & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(job.inputFormat.category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = job.inputFormat.category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.originalFileName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${job.inputFormat.displayName} ➔ ${job.targetFormat.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FileeCyan
                    )
                }

                // Status indicator pill
                StatusBadge(status = job.status)

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp).testTag("job_remove_button_${job.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove job",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar / Step description
            when (job.status) {
                JobStatus.CONVERTING, JobStatus.ROUTING -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = job.currentStepText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "${(job.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = FileeCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { job.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = FileeCyan,
                            trackColor = FileeNavySurfaceVariant
                        )
                    }
                }
                JobStatus.SUCCESS -> {
                    // Size & Duration Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FileeNavySurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Size: ${FileUtils.formatFileSize(job.sourceSizeBytes)} ➔ ${FileUtils.formatFileSize(job.resultSizeBytes)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Took ${job.durationMs}ms offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        if (job.sizeDeltaPercent != 0) {
                            val isSaved = job.sizeDeltaPercent < 0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSaved) FileeEmerald.copy(alpha = 0.2f)
                                        else Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${if (job.sizeDeltaPercent > 0) "+" else ""}${job.sizeDeltaPercent}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSaved) FileeEmerald else Color(0xFFF59E0B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons (Download, Open & Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier
                                .weight(1.1f)
                                .height(40.dp)
                                .testTag("btn_download_${job.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FileeEmerald,
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = onOpen,
                            modifier = Modifier
                                .weight(0.9f)
                                .height(40.dp)
                                .testTag("btn_open_${job.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = FileeNavySurfaceVariant,
                                contentColor = FileeCyan
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open", fontSize = 11.sp)
                        }

                        FilledTonalButton(
                            onClick = onShare,
                            modifier = Modifier
                                .weight(0.9f)
                                .height(40.dp)
                                .testTag("btn_share_${job.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = FileeNavySurfaceVariant,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 11.sp)
                        }
                    }
                }
                JobStatus.FAILED -> {
                    Column {
                        Text(
                            text = job.errorMessage ?: "Conversion failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = FileeRose
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onRetry,
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FileeRose)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 11.sp)
                        }
                    }
                }
                JobStatus.PENDING -> {
                    Text(
                        text = "Queued • Ready to convert along route",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                JobStatus.CANCELLED -> {
                    Text(
                        text = "Cancelled by user",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: JobStatus) {
    val (bgColor, textColor, label) = when (status) {
        JobStatus.PENDING -> Triple(Color(0xFF334155), Color(0xFF94A3B8), "Pending")
        JobStatus.ROUTING, JobStatus.CONVERTING -> Triple(FileeCyan.copy(alpha = 0.2f), FileeCyan, "Working...")
        JobStatus.SUCCESS -> Triple(FileeEmerald.copy(alpha = 0.2f), FileeEmerald, "Ready")
        JobStatus.FAILED -> Triple(FileeRose.copy(alpha = 0.2f), FileeRose, "Failed")
        JobStatus.CANCELLED -> Triple(Color(0xFF334155), Color(0xFF94A3B8), "Cancelled")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            fontSize = 10.sp
        )
    }
}
