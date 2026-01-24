package com.example.waterreminder.ui.goals

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.ui.components.ScreenHeader
import com.example.waterreminder.ui.theme.TextSecondary
import com.example.waterreminder.util.UnitSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun GoalsScreen(viewModel: GoalsViewModel, preferencesManager: PreferencesManager) {
    val state by viewModel.uiState.collectAsState()
    val storedScroll by preferencesManager.goalsScroll.collectAsState(initial = 0)
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
                    preferencesManager.setGoalsScroll(scrollState.value)
                }
            }
    }
    LaunchedEffect(state.onboardingShown) {
        if (!state.onboardingShown) {
            delay(2400)
            viewModel.markOnboardingShown()
        }
    }
    val displayGoal = state.dailyGoalMl.toFloat()
    val cupsGoal = if (state.cupSizeMl > 0) state.dailyGoalMl / state.cupSizeMl else 0
    val sliderEnabled = !state.adaptive
    val minLabel = "1000 ml"
    val maxLabel = "4000 ml"
    val displayGoalLabel = "${displayGoal.toInt()} ml"
    val summaryText = "${cupsGoal} cups/day · ${state.cupSizeMl} ml per cup · 9 AM–9 PM"
    val step = 250
    val minGoal = 1000
    val maxGoal = 4000

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Goals")

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Daily Goal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = displayGoalLabel, fontWeight = FontWeight.SemiBold)
                        if (state.adaptive) {
                            Icon(imageVector = Icons.Outlined.Lock, contentDescription = "Locked", tint = TextSecondary)
                        }
                    }
                }
                if (!state.onboardingShown) {
                    OnboardingTip(text = "Adjust your daily goal here.")
                }
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 18.dp)) {
                    Text(
                        text = "Locked while Adaptive Goals is enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.alpha(if (state.adaptive) 1f else 0f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val next = (displayGoal.toInt() - step).coerceAtLeast(minGoal)
                            val ml = next
                            viewModel.updateDailyGoal(ml)
                        },
                        enabled = sliderEnabled && displayGoal.toInt() > minGoal
                    ) {
                        Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Decrease")
                    }
                    Text(text = displayGoalLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = {
                            val next = (displayGoal.toInt() + step).coerceAtMost(maxGoal)
                            val ml = next
                            viewModel.updateDailyGoal(ml)
                        },
                        enabled = sliderEnabled && displayGoal.toInt() < maxGoal
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Increase")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = minLabel, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(text = maxLabel, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
            }
        }

        Text(text = "Active Hours", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(text = "8:00 AM – 5:00 PM", modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(text = "9:00 AM - 9:00 PM", modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }
        }

        Text(text = "Cup Size", style = MaterialTheme.typography.titleLarge)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${state.cupSizeMl} ml", fontWeight = FontWeight.SemiBold)
                Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = "Select cup", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Adaptive Goals", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = state.adaptive,
                        onCheckedChange = { viewModel.updateAdaptive(it) },
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    )
                }
                Text(
                    text = "Automatically adjusts your daily goal based on past intake.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Column(modifier = Modifier.alpha(if (state.adaptive) 1f else 0f)) {
                    Text(
                        text = "Smart Recommendation",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Based on your last 7 days, try 9 cups.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Text(text = "Summary", style = MaterialTheme.typography.titleLarge)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = summaryText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(text = "Changes are saved automatically.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
