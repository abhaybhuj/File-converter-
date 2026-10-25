package com.example.filee.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.ui.components.ConversionSettingsSheet
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import com.example.filee.ui.viewmodel.FileeViewModel
import java.io.File

@Composable
fun SettingsScreen(
    viewModel: FileeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val conversionOptions by viewModel.conversionOptions.collectAsState()
    var showTuningSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_settings")
    ) {
        Text(
            text = "App Settings & About",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Configuration, engine status, and offline security assurance.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Security & Privacy Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FileeNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FileeEmerald.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FileeEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = FileeEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Zero-Cloud Guarantee",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "100% On-Device Processing",
                            style = MaterialTheme.typography.labelSmall,
                            color = FileeEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No files, bytes, or personal telemetry ever leave this device. Conversions run via local Android hardware decoders, PDF graphics engines, and algorithmic text/data parsers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Engine Tuning Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FileeNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ENGINE PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                    color = FileeCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Image & PDF Tuning",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Quality: ${conversionOptions.imageQuality}% • Scale: ${conversionOptions.resizePercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    FilledTonalButton(
                        onClick = { showTuningSheet = true },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = FileeNavySurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = FileeCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configure", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Storage & Cache Cleanup
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FileeNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "STORAGE & CACHE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                    color = FileeCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Converted Output Files",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Clear generated files in temporary cache",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val dir = File(context.filesDir, "converted")
                            val count = dir.listFiles()?.size ?: 0
                            dir.deleteRecursively()
                            dir.mkdirs()
                            Toast.makeText(context, "Cleaned $count converted files", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = FileeNavySurfaceVariant),
                        modifier = Modifier.testTag("btn_clean_cache")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clean Cache", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // About Filee Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FileeNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = FileeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About Filee",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Filee for Android is a native mobile implementation of KnifeLemon/Filee (originally for Windows/Desktop under MIT License).\n\n" +
                            "Features brought to Android:\n" +
                            "• Radial Donut format picker with multi-category arc navigation\n" +
                            "• BFS Multi-hop conversion pathfinder (up to 3 hops)\n" +
                            "• Native Android engines for images, documents, structured data, audio synthesis, and archives\n" +
                            "• Room local persistence for history logs\n" +
                            "• Direct Android Share Sheet and FileProvider integration",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showTuningSheet) {
        ConversionSettingsSheet(
            initialOptions = conversionOptions,
            onSave = { viewModel.updateConversionOptions(it) },
            onDismiss = { showTuningSheet = false }
        )
    }
}
