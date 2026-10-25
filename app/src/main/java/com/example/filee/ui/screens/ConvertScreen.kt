package com.example.filee.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.filee.data.model.FileFormat
import com.example.filee.data.model.JobStatus
import com.example.filee.ui.components.ConversionItemCard
import com.example.filee.ui.components.ConversionSettingsSheet
import com.example.filee.ui.components.FileeDonutMenu
import com.example.filee.ui.components.RouteVisualizer
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import com.example.filee.ui.viewmodel.FileeViewModel
import com.example.filee.util.FileUtils
import com.example.filee.util.SampleFilesUtil

@Composable
fun ConvertScreen(
    viewModel: FileeViewModel,
    onNavigateToBatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedFile by viewModel.selectedFile.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableTargetFormats by viewModel.availableTargetFormats.collectAsState()
    val selectedTargetFormat by viewModel.selectedTargetFormat.collectAsState()
    val currentRoute by viewModel.currentRoute.collectAsState()
    val activeJob by viewModel.activeJob.collectAsState()
    val conversionOptions by viewModel.conversionOptions.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }

    // System File Picker using ActivityResultContracts.GetMultipleContents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            if (uris.size == 1) {
                val uri = uris.first()
                val fileName = FileUtils.getFileNameFromUri(context, uri)
                val bytes = FileUtils.readBytesFromUri(context, uri)
                if (bytes != null) {
                    val format = FileFormat.fromFileName(fileName) ?: FileFormat.TXT
                    viewModel.selectFile(fileName, bytes, format, uri)
                }
            } else {
                // Multiple files selected -> queue directly for batch processing!
                val selectedFiles = uris.mapNotNull { uri ->
                    val name = FileUtils.getFileNameFromUri(context, uri)
                    val bytes = FileUtils.readBytesFromUri(context, uri)
                    if (bytes != null) {
                        val format = FileFormat.fromFileName(name) ?: FileFormat.TXT
                        com.example.filee.ui.viewmodel.SelectedFile(name, format, bytes, uri)
                    } else null
                }
                if (selectedFiles.isNotEmpty()) {
                    viewModel.addFilesToBatch(selectedFiles)
                    onNavigateToBatch()
                }
            }
        }
    }

    val sampleFiles = remember { SampleFilesUtil.getSampleFiles() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_convert")
    ) {
        // App Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Filee Converter",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Offline • Multi-Hop Routing • 250+ Formats",
                    style = MaterialTheme.typography.labelSmall,
                    color = FileeCyan
                )
            }

            IconButton(
                onClick = { showSettingsSheet = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FileeNavySurfaceVariant)
                    .testTag("btn_open_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings",
                    tint = FileeCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input File Selection Drop Zone
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { filePickerLauncher.launch("*/*") }
                .testTag("file_picker_dropzone"),
            shape = RoundedCornerShape(20.dp),
            color = FileeNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, FileeCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedFile == null) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = FileeCyan,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to Pick File from Storage",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Supports images, documents, CSV, JSON, audio, archives",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    val file = selectedFile!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(file.format.category.color.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = file.format.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = file.format.category.color
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${file.format.category.displayName} • ${FileUtils.formatFileSize(file.bytes.size.toLong())}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        FilledTonalButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = FileeNavySurfaceVariant)
                        ) {
                            Text("Change", fontSize = 11.sp, color = FileeCyan)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Sample Files Bar (Instant 1-tap testing)
        Text(
            text = "OR TEST WITH SAMPLE FILES:",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleFiles.forEach { sample ->
                val isSelected = selectedFile?.name == sample.name
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) sample.format.category.color.copy(alpha = 0.3f) else FileeNavySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) sample.format.category.color else FileeNavyBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.selectFile(sample.name, sample.bytes, sample.format) }
                        .testTag("sample_chip_${sample.format.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ".${sample.format.extension}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = sample.format.category.color
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sample.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Filee Donut Menu: Radial format selector
        if (selectedFile != null) {
            FileeDonutMenu(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                availableFormats = availableTargetFormats,
                selectedFormat = selectedTargetFormat,
                onFormatSelected = { viewModel.selectTargetFormat(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conversion Route Visualizer
            currentRoute?.let { route ->
                RouteVisualizer(route = route)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Bar: Convert Button and Add to Batch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val file = selectedFile
                        val target = selectedTargetFormat
                        if (file != null && target != null) {
                            viewModel.addFilesToBatch(listOf(file), target)
                            onNavigateToBatch()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_add_to_batch"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Batch", fontSize = 13.sp)
                }

                Button(
                    onClick = { viewModel.startSingleConversion() },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("btn_convert_now"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FileeCyan,
                        contentColor = Color(0xFF0F172A)
                    ),
                    enabled = activeJob?.status != JobStatus.CONVERTING
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeJob?.status == JobStatus.CONVERTING) "Converting..." else "Convert Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Active Conversion Status Card
        activeJob?.let { job ->
            Spacer(modifier = Modifier.height(16.dp))
            ConversionItemCard(
                job = job,
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
                onRemove = { /* Active job dismiss */ },
                onRetry = { viewModel.startSingleConversion() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showSettingsSheet) {
        ConversionSettingsSheet(
            initialOptions = conversionOptions,
            onSave = { viewModel.updateConversionOptions(it) },
            onDismiss = { showSettingsSheet = false }
        )
    }
}
