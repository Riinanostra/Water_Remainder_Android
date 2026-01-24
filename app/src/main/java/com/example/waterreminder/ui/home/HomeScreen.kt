package com.example.waterreminder.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.ui.components.PrimaryButton
import com.example.waterreminder.ui.components.ScreenHeader
import com.example.waterreminder.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun HomeScreen(viewModel: HomeViewModel, preferencesManager: PreferencesManager) {
    val state by viewModel.uiState.collectAsState()
    val storedScroll by preferencesManager.homeScroll.collectAsState(initial = 0)
    val scrollState = rememberScrollState()
    val haptics = LocalHapticFeedback.current
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
                    preferencesManager.setHomeScroll(scrollState.value)
                }
            }
    }
    val cupsToday = if (state.cupSizeMl > 0) state.todayIntakeMl / state.cupSizeMl else 0
    val cupsGoal = if (state.cupSizeMl > 0) state.dailyGoalMl / state.cupSizeMl else 0
    val percent = (state.progress * 100).toInt().coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "homeProgress"
    )
    val animatedRingColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 600),
        label = "ringColor"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Home")

        Text(text = "Stay hydrated today", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Set reminders and progress.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

        val ringTrack = MaterialTheme.colorScheme.outlineVariant
        val ringProgress = animatedRingColor
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(210.dp)) {
                    Canvas(modifier = Modifier.size(210.dp)) {
                        val stroke = 14.dp.toPx()
                        val diameter = size.minDimension
                        drawArc(
                            color = ringTrack,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                            size = Size(diameter, diameter),
                            topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                        )
                        drawArc(
                            color = ringProgress.copy(alpha = 0.25f),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = stroke + 8.dp.toPx(), cap = StrokeCap.Round),
                            size = Size(diameter, diameter),
                            topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                        )
                        drawArc(
                            color = ringProgress,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                            size = Size(diameter, diameter),
                            topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$cupsToday / $cupsGoal",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(text = "cups", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(text = "$percent% of daily goal", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }

                PrimaryButton(
                    label = "Drink water",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.logWater()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = state.nextReminder, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🔥", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "${state.streakDays} day streak", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(text = "Keep it up!", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
    }
}
