package com.example.waterreminder.ui.history

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.ui.components.SecondaryButton
import com.example.waterreminder.ui.components.ScreenHeader
import com.example.waterreminder.ui.theme.TextSecondary
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, preferencesManager: PreferencesManager) {
    val state by viewModel.uiState.collectAsState()
    val storedScroll by preferencesManager.historyScroll.collectAsState(initial = 0)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val hasData = state.chartEntries.any { it.totalMl > 0 } || state.heatmapDays.any { it.intensity > 0f }

    LaunchedEffect(storedScroll) {
        if (scrollState.value == 0 && storedScroll > 0) {
            scrollState.scrollTo(storedScroll)
        }
    }
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { inProgress ->
                if (!inProgress) {
                    preferencesManager.setHistoryScroll(scrollState.value)
                }
            }
    }
    LaunchedEffect(state.exportStatus) {
        if (state.exportStatus.isNotEmpty()) {
            Toast.makeText(context, state.exportStatus, Toast.LENGTH_SHORT).show()
            viewModel.clearExportStatus()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "History")

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabPill("Weekly", selected = state.weekView) { viewModel.toggleWeekView(true) }
                    TabPill("Monthly", selected = !state.weekView) { viewModel.toggleWeekView(false) }
                }
                if (hasData) {
                    HistoryChart(entries = state.chartEntries)
                } else {
                    EmptyStateCard(
                        title = "No history yet",
                        message = "Log your first drink to see weekly trends."
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Outlined.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFF8A00))
                    Text(text = "Best streak", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = "Calendar", tint = TextSecondary)
                    Text(text = if (hasData) state.streakSummary else "No streak yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Heatmap", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (hasData) {
                    HeatmapGrid(state.heatmapDays)
                } else {
                    EmptyStateCard(
                        title = "No activity yet",
                        message = "Your heatmap will appear after a few days."
                    )
                }
            }
        }

        SecondaryButton(
            label = "Export CSV",
            onClick = { viewModel.exportCsv() },
            modifier = Modifier.align(Alignment.End),
            leadingIcon = Icons.Outlined.CloudDownload
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun HeatmapGrid(days: List<HeatmapDay>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = 7,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach { day ->
            val color = when {
                day.intensity >= 0.9f -> MaterialTheme.colorScheme.primary
                day.intensity >= 0.6f -> Color(0xFF7DB8FF)
                day.intensity >= 0.3f -> Color(0xFFBBD9FF)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Column(
                modifier = Modifier
                    .size(18.dp)
                    .background(color = color, shape = RoundedCornerShape(6.dp))
            ) {}
        }
    }
}

@Composable
private fun TabPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) Color.White else TextSecondary
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = background,
        onClick = onClick,
        modifier = Modifier.heightIn(min = 48.dp)
    ) {
        Text(text = label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = textColor)
    }
}

@Composable
private fun EmptyStateCard(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
