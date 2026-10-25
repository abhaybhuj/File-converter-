package com.example.filee.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.model.ConversionRoute
import com.example.filee.data.model.FileFormat
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeEmerald
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteVisualizer(
    route: ConversionRoute,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("route_visualizer_card"),
        shape = RoundedCornerShape(18.dp),
        color = FileeNavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, FileeNavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title and Route Type Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = null,
                        tint = FileeCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONVERSION ROUTE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }

                // Tag badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (route.isDirect) FileeEmerald.copy(alpha = 0.2f)
                            else FileeCyan.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (route.isDirect) "DIRECT 1-HOP" else "SMART ${route.hopCount}-HOPS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (route.isDirect) FileeEmerald else FileeCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Hops Node Flow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center
            ) {
                for (i in route.hops.indices) {
                    val format = route.hops[i]
                    val isFirst = i == 0
                    val isLast = i == route.hops.size - 1

                    FormatNodeBadge(
                        format = format,
                        isEndpoint = isFirst || isLast,
                        isTarget = isLast
                    )

                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fidelity & Strategy Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FileeNavySurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = FileeCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fidelity: ${route.estimatedFidelity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
fun FormatNodeBadge(
    format: FileFormat,
    isEndpoint: Boolean,
    isTarget: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isTarget -> format.category.color.copy(alpha = 0.25f)
            isEndpoint -> FileeNavySurfaceVariant
            else -> FileeNavyDark
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isTarget) 1.5.dp else 1.dp,
            color = if (isTarget) format.category.color else FileeNavyBorder
        ),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(format.category.color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = format.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = ".${format.extension}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }
        }
    }
}
