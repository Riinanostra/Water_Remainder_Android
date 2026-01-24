package com.example.waterreminder.ui.profile

import android.Manifest
import android.widget.Toast
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.ui.components.SecondaryButton
import com.example.waterreminder.ui.components.ScreenHeader
import com.example.waterreminder.util.ThemeMode
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, preferencesManager: PreferencesManager) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val storedScroll by preferencesManager.profileScroll.collectAsState(initial = 0)
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
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
                    preferencesManager.setProfileScroll(scrollState.value)
                }
            }
    }
    LaunchedEffect(state.exportStatus) {
        if (state.exportStatus.isNotEmpty()) {
            Toast.makeText(context, state.exportStatus, Toast.LENGTH_SHORT).show()
            viewModel.clearExportStatus()
        }
    }
    LaunchedEffect(state.syncStatus) {
        if (state.syncStatus.isNotEmpty()) {
            Toast.makeText(context, state.syncStatus, Toast.LENGTH_SHORT).show()
            viewModel.clearSyncStatus()
        }
    }
    LaunchedEffect(state.resetStatus) {
        if (state.resetStatus.isNotEmpty()) {
            Toast.makeText(context, state.resetStatus, Toast.LENGTH_SHORT).show()
            if (state.resetStatus == "Daily intake reset") {
                val result = snackbarHostState.showSnackbar(
                    message = "Daily intake reset",
                    actionLabel = "Undo"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoResetDailyIntake()
                }
            }
            viewModel.clearResetStatus()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(title = "More")

            SectionCard(
                title = "Preferences",
                icon = Icons.Outlined.Settings
            ) {
                LabelRow("Theme", Icons.Outlined.LightMode)
                ThemeSegmentedControl(selected = state.themeMode, onSelect = viewModel::updateTheme)
                SpacerRow()
                LabelRow("Units", Icons.Outlined.DarkMode)
                Text(text = "ml", style = MaterialTheme.typography.bodyLarge)
            }

            SectionCard(
                title = "Notifications",
                icon = Icons.Outlined.NotificationsActive
            ) {
                SecondaryButton(
                    label = "Grant Permission",
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(
                title = "Daily Intake",
                icon = Icons.Outlined.Restore
            ) {
                SecondaryButton(
                    label = "Reset Daily Intake",
                    onClick = { viewModel.resetDailyIntake() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(
                title = "Data Export",
                icon = Icons.Outlined.Download
            ) {
                SecondaryButton(
                    label = "Export to Downloads",
                    onClick = { viewModel.exportData() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard(
                title = "Server Sync",
                icon = Icons.Outlined.CloudSync
            ) {
                SecondaryButton(
                    label = "Sync Now",
                    onClick = { viewModel.syncNow() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(imageVector = icon, contentDescription = null)
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
            content()
        }
    }
}

@Composable
private fun LabelRow(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SpacerRow() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ThemeSegmentedControl(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to "System",
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark"
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size)
            ) {
                Text(text = label)
            }
        }
    }
}

