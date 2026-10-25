package com.example.filee.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.model.FileFormat
import com.example.filee.data.model.FormatCategory
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeNavyBorder
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Filee Signature Radial / Donut Format Selector.
 * Renders an interactive circular ring of format categories with direct format chips inside.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileeDonutMenu(
    selectedCategory: FormatCategory,
    onCategorySelected: (FormatCategory) -> Unit,
    availableFormats: List<FileFormat>,
    selectedFormat: FileFormat?,
    onFormatSelected: (FileFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember { FormatCategory.entries.toTypedArray() }
    var hoveredCategory by remember { mutableStateOf<FormatCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FileeNavySurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Donut Title & Category Indicator
        Text(
            text = "FILEE RADIAL SELECTOR",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FileeCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Radial Donut Canvas
        BoxWithConstraints(
            modifier = Modifier
                .size(240.dp)
                .testTag("donut_wheel_container"),
            contentAlignment = Alignment.Center
        ) {
            val canvasSizePx = constraints.maxWidth.toFloat()
            val center = Offset(canvasSizePx / 2f, canvasSizePx / 2f)
            val outerRadius = canvasSizePx / 2f - 16f
            val innerRadius = outerRadius - 42f
            val strokeWidth = 36f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                            if (dist in (innerRadius - 15f)..(outerRadius + 20f)) {
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f
                                val sweep = 360f / categories.size
                                val index = ((angle / sweep).toInt()).coerceIn(0, categories.size - 1)
                                onCategorySelected(categories[index])
                            }
                        }
                    }
            ) {
                val sweep = 360f / categories.size

                for (i in categories.indices) {
                    val cat = categories[i]
                    val startAngle = i * sweep
                    val isSelected = cat == selectedCategory
                    val arcStroke = if (isSelected) strokeWidth + 8f else strokeWidth

                    // Draw outer arc sector
                    drawArc(
                        color = if (isSelected) cat.color else cat.color.copy(alpha = 0.35f),
                        startAngle = startAngle + 2f,
                        sweepAngle = sweep - 4f,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius + (if (isSelected) 0f else 4f), center.y - outerRadius + (if (isSelected) 0f else 4f)),
                        size = Size((outerRadius - (if (isSelected) 0f else 4f)) * 2, (outerRadius - (if (isSelected) 0f else 4f)) * 2),
                        style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                    )
                }

                // Inner circle backdrop
                drawCircle(
                    color = FileeNavyDark,
                    radius = innerRadius - 10f,
                    center = center
                )
                drawCircle(
                    color = selectedCategory.color.copy(alpha = 0.15f),
                    radius = innerRadius - 10f,
                    center = center
                )
            }

            // Center Content: Selected Category Information
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable { /* Tapping center cycles category */
                        val nextIdx = (selectedCategory.ordinal + 1) % categories.size
                        onCategorySelected(categories[nextIdx])
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    tint = selectedCategory.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedCategory.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${availableFormats.count { it.category == selectedCategory }} targets",
                    style = MaterialTheme.typography.labelSmall,
                    color = selectedCategory.color,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(cat) },
                    label = {
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = cat.color.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White,
                        containerColor = FileeNavySurfaceVariant,
                        labelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .testTag("category_chip_${cat.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Target Formats inside selected category
        val categoryFormats = availableFormats.filter { it.category == selectedCategory }
        if (categoryFormats.isNotEmpty()) {
            Text(
                text = "Compatible ${selectedCategory.displayName} Formats:",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFCBD5E1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryFormats.forEach { format ->
                    val isSelected = format == selectedFormat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) selectedCategory.color else FileeNavySurfaceVariant,
                        tonalElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFormatSelected(format) }
                            .testTag("format_pill_${format.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = format.displayName,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) Color(0xFF0F172A) else Color.White
                            )
                            Text(
                                text = ".${format.extension}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
