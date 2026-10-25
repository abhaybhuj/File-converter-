package com.example.filee.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.model.ConversionOptions
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionSettingsSheet(
    initialOptions: ConversionOptions,
    onSave: (ConversionOptions) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var quality by remember { mutableFloatStateOf(initialOptions.imageQuality.toFloat()) }
    var resizeScale by remember { mutableFloatStateOf(initialOptions.resizePercentage.toFloat()) }
    var isLandscape by remember { mutableStateOf(initialOptions.pdfOrientationLandscape) }
    var prettyJson by remember { mutableStateOf(initialOptions.prettyPrintJson) }
    var delimiter by remember { mutableStateOf(initialOptions.csvDelimiter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FileeNavySurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("conversion_settings_sheet")
        ) {
            Text(
                text = "Conversion Engine Tuning",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "Adjust offline encoding quality, layout, and formatting parameters.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Image Quality Slider
            Text(
                text = "Image Compression Quality: ${quality.toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Slider(
                value = quality,
                onValueChange = { quality = it },
                valueRange = 20f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = FileeCyan,
                    activeTrackColor = FileeCyan,
                    inactiveTrackColor = Color(0xFF334155)
                ),
                modifier = Modifier.testTag("slider_image_quality")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Resize Scale
            Text(
                text = "Image Dimensions Resize: ${resizeScale.toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Slider(
                value = resizeScale,
                onValueChange = { resizeScale = it },
                valueRange = 25f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = FileeCyan,
                    activeTrackColor = FileeCyan,
                    inactiveTrackColor = Color(0xFF334155)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PDF Orientation Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PDF Landscape Orientation",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = if (isLandscape) "Horizontal (842x595 pt)" else "Portrait (595x842 pt)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Switch(
                    checked = isLandscape,
                    onCheckedChange = { isLandscape = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = FileeCyan)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pretty JSON Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Format & Indent JSON/XML",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Adds linebreaks and indentation for readability",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Switch(
                    checked = prettyJson,
                    onCheckedChange = { prettyJson = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = FileeCyan)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CSV Delimiter Chips
            Text(
                text = "CSV/Tabular Delimiter",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(',' to "Comma (,)", ';' to "Semicolon (;)", '\t' to "Tab (\\t)").forEach { (char, label) ->
                    FilterChip(
                        selected = delimiter == char,
                        onClick = { delimiter = char },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSave(
                        initialOptions.copy(
                            imageQuality = quality.toInt(),
                            resizePercentage = resizeScale.toInt(),
                            pdfOrientationLandscape = isLandscape,
                            prettyPrintJson = prettyJson,
                            csvDelimiter = delimiter
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_settings"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FileeCyan,
                    contentColor = Color(0xFF0F172A)
                )
            ) {
                Text("Apply Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}
