package com.example.filee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.model.FileFormat
import com.example.filee.ui.components.RouteVisualizer
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import com.example.filee.ui.viewmodel.FileeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteExplorerScreen(
    viewModel: FileeViewModel,
    modifier: Modifier = Modifier
) {
    val sourceFormat by viewModel.explorerSource.collectAsState()
    val targetFormat by viewModel.explorerTarget.collectAsState()
    val exploredRoute by viewModel.exploredRoute.collectAsState()

    val allFormats = remember { FileFormat.entries }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_route_explorer")
    ) {
        Text(
            text = "Route Pathfinder",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Explore Filee's multi-hop conversion graph across 250+ format paths.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Selector Bar: Source & Target
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FileeNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Source Selector
                Text(
                    text = "SOURCE FORMAT",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                    color = FileeCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allFormats.take(12).forEach { format ->
                        val isSelected = format == sourceFormat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setExplorerSource(format) },
                            label = { Text(format.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = format.category.color.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White,
                                containerColor = FileeNavySurfaceVariant,
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Swap Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            val temp = sourceFormat
                            viewModel.setExplorerSource(targetFormat)
                            viewModel.setExplorerTarget(temp)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FileeNavySurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Formats",
                            tint = FileeCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target Selector
                Text(
                    text = "TARGET FORMAT",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                    color = FileeEmerald
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allFormats.take(12).forEach { format ->
                        val isSelected = format == targetFormat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setExplorerTarget(format) },
                            label = { Text(format.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = format.category.color.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White,
                                containerColor = FileeNavySurfaceVariant,
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Explored Route Visualizer
        if (exploredRoute != null) {
            RouteVisualizer(route = exploredRoute!!)
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FileeNavySurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No route found within 3 hops between ${sourceFormat.displayName} and ${targetFormat.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Popular Routes Showcase
        Text(
            text = "SIGNATURE FILEE CONVERSIONS",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val popularRoutes = listOf(
            Triple(FileFormat.CSV, FileFormat.PDF, "Tabular sheet converted to formatted PDF document"),
            Triple(FileFormat.MD, FileFormat.PDF, "Markdown styled and rendered into crisp PDF"),
            Triple(FileFormat.SVG, FileFormat.WEBP, "Scalable vector rasterized into modern WebP"),
            Triple(FileFormat.PDF, FileFormat.PNG, "Extract high-resolution image from PDF"),
            Triple(FileFormat.TXT, FileFormat.WAV, "Text synthesized into offline PCM audio waveform"),
            Triple(FileFormat.JSON, FileFormat.YAML, "Data structured tree mapped to human-readable YAML"),
            Triple(FileFormat.CSV, FileFormat.JSON, "CSV rows automatically parsed to JSON array")
        )

        popularRoutes.forEach { (src, tgt, desc) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FileeNavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.setExplorerSource(src)
                        viewModel.setExplorerTarget(tgt)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = src.displayName,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = src.category.color
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tgt.displayName,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = tgt.category.color
                            )
                        }
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = null,
                        tint = FileeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
