package com.example.waterreminder.ui.reminders

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterreminder.ui.theme.TextSecondary
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.ui.components.ScreenHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RemindersScreen(viewModel: RemindersViewModel, preferencesManager: PreferencesManager) {
    val state by viewModel.uiState.collectAsState()
    val storedScroll by preferencesManager.remindersScroll.collectAsState(initial = 0)
    val scrollState = rememberScrollState()
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
                    preferencesManager.setRemindersScroll(scrollState.value)
                }
            }
    }
    val startHour = state.startTime.hour + state.startTime.minute / 60f
    val endHour = state.endTime.hour + state.endTime.minute / 60f
    var sliderValues by remember(state.startTime, state.endTime) { mutableStateOf(startHour..endHour) }
    LaunchedEffect(state.onboardingShown) {
        if (!state.onboardingShown) {
            delay(2400)
            viewModel.markOnboardingShown()
        }
    }

    val controlsEnabled = state.enabled
    val summaryText = "You'll be reminded every ${state.intervalMinutes} min between ${formatTime(state.startTime)}–${formatTime(state.endTime)}"

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Reminders")

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 4.dp)) { }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (state.enabled) "Reminders on" else "Reminders off", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { viewModel.toggleEnabled(it) },
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    )
                }
                if (!state.onboardingShown) {
                    OnboardingTip(text = "Enable reminders to stay on schedule.")
                }

                Text(text = summaryText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                Text(text = "Reminder Interval", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                IntervalSegmentedControl(
                    selectedMinutes = state.intervalMinutes,
                    enabled = controlsEnabled,
                    onSelect = viewModel::updateInterval
                )

                Text(text = "Active Hours", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(state.startTime), style = MaterialTheme.typography.labelLarge)
                    Text(text = formatTime(state.endTime), style = MaterialTheme.typography.labelLarge)
                }
                RangeSlider(
                    value = sliderValues,
                    onValueChange = { sliderValues = it },
                    onValueChangeFinished = {
                        val start = sliderValues.start
                        val end = sliderValues.endInclusive
                        val startTime = LocalTime.of(start.toInt(), ((start % 1) * 60).toInt())
                        val endTime = LocalTime.of(end.toInt(), ((end % 1) * 60).toInt())
                        viewModel.updateStartTime(startTime)
                        viewModel.updateEndTime(endTime)
                    },
                    valueRange = 0f..24f,
                    enabled = controlsEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        activeTrackColor = if (controlsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        thumbColor = if (controlsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                )
                if (!state.enabled) {
                    Text(
                        text = "Turn on reminders to edit interval and hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Text(text = "Smart Reminders", style = MaterialTheme.typography.titleLarge)
        Text(
            text = if (state.enabled) "Smart reminders follow your context." else "Enable reminders to use smart modes.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        SmartReminderCard(
            icon = Icons.Outlined.Work,
            title = "Office Mode",
            description = "Detected via motion + location",
            enabled = state.smartOffice,
            onToggle = { viewModel.updateSmartOffice(it) },
            updating = false,
            parentEnabled = controlsEnabled
        )
        SmartReminderCard(
            icon = Icons.Outlined.FitnessCenter,
            title = "Workout Mode",
            description = "Detected via motion + workout patterns",
            enabled = state.smartWorkout,
            onToggle = { viewModel.updateSmartWorkout(it) },
            updating = false,
            parentEnabled = controlsEnabled
        )

        Text(text = "Advanced", style = MaterialTheme.typography.titleLarge)
        SmartReminderCard(
            icon = Icons.Outlined.Flight,
            title = "Travel Mode",
            description = "Adjust reminders when time zone changes",
            enabled = state.smartTravel,
            onToggle = { viewModel.updateSmartTravel(it) },
            updating = false,
            parentEnabled = controlsEnabled,
            compact = true
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun IntervalSegmentedControl(selectedMinutes: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    val options = listOf(15 to "15 min", 30 to "30 min", 60 to "1 hour")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (minutes, label) ->
            SegmentedButton(
                selected = selectedMinutes == minutes,
                onClick = { onSelect(minutes) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index, options.size)
            ) {
                Text(text = label)
            }
        }
    }
}

@Composable
private fun SmartReminderCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    updating: Boolean,
    parentEnabled: Boolean,
    compact: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 1.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (compact) 12.dp else 16.dp,
                    top = if (compact) 12.dp else 16.dp,
                    end = if (compact) 24.dp else 32.dp,
                    bottom = if (compact) 12.dp else 16.dp
                )
                .alpha(if (updating) 0.6f else if (parentEnabled) 1f else 0.5f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val tint = when {
                        enabled && parentEnabled -> MaterialTheme.colorScheme.primary
                        parentEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                    Icon(imageVector = icon, contentDescription = title, tint = tint)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2
                    )
                    Text(
                        text = if (enabled && parentEnabled) "Status: Active" else "Status: Off",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .sizeIn(minWidth = 56.dp)
            ) {
                if (updating) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        enabled = parentEnabled && !updating,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingTip(text: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTime(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    return time.format(formatter)
}
