package com.example.waterreminder.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterreminder.ui.theme.TextSecondary

@Composable
fun HistoryChart(entries: List<ChartEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) {
        return
    }
    val maxValue = entries.maxOfOrNull { it.totalMl }?.coerceAtLeast(1) ?: 1
    val barColor = MaterialTheme.colorScheme.primary
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height(140.dp)) {
                Text(text = "7", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(text = "6", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(text = "5", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(text = "4", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
            ) {
                val barWidth = size.width / (entries.size * 1.6f)
                entries.forEachIndexed { index, entry ->
                    val barHeight = (entry.totalMl.toFloat() / maxValue) * size.height
                    val x = index * (barWidth * 1.6f)
                    val y = size.height - barHeight
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(text = day, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
