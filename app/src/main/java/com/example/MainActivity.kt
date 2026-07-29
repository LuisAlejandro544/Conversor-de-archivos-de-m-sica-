package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AudioViewModel
import com.example.ui.components.AudioPlayerBottomBar
import com.example.ui.components.ConversionProgressDialog
import com.example.ui.components.StudioHeaderBar
import com.example.ui.screens.AudioInspectorScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.FilesHistoryScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.theme.AudioLabsTheme
import com.example.ui.theme.StudioCobalt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioLabsTheme {
                AudioLabsMainApp()
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun AudioLabsMainApp(viewModel: AudioViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val selectedTrack by viewModel.selectedTrack.collectAsStateWithLifecycle()
    val sampleTracks by viewModel.sampleTracks.collectAsStateWithLifecycle()
    val conversionConfig by viewModel.conversionConfig.collectAsStateWithLifecycle()
    val conversionState by viewModel.conversionState.collectAsStateWithLifecycle()
    val historyAudios by viewModel.historyAudios.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterFormat by viewModel.filterFormat.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    val navTabs = listOf(
        NavTabItem("Conversor", Icons.Default.SwapHoriz, "tab_converter"),
        NavTabItem("Presets", Icons.Default.Tune, "tab_presets"),
        NavTabItem("Mis Archivos", Icons.Default.FolderZip, "tab_files"),
        NavTabItem("Inspector", Icons.Default.Analytics, "tab_inspector")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            StudioHeaderBar(historyCount = historyAudios.size)
        },
        bottomBar = {
            Column {
                AudioPlayerBottomBar(
                    state = playerState,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onStop = { viewModel.playerManager.stop() }
                )

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navTabs.forEachIndexed { index, item ->
                        val isSelected = activeTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setActiveTab(index) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) StudioCobalt else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) StudioCobalt else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = StudioCobalt.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> ConverterScreen(
                    selectedTrack = selectedTrack,
                    sampleTracks = sampleTracks,
                    config = conversionConfig,
                    onSelectSampleTrack = { viewModel.selectTrack(it) },
                    onCustomAudioPicked = { viewModel.inspectCustomUri(it) },
                    onSetTargetFormat = { viewModel.setTargetFormat(it) },
                    onSetBitrate = { viewModel.setBitrate(it) },
                    onSetSampleRate = { viewModel.setSampleRate(it) },
                    onSetChannels = { viewModel.setChannels(it) },
                    onSetVolumeGain = { viewModel.setVolumeGain(it) },
                    onSetTrimRange = { start, end, enabled -> viewModel.setTrimRange(start, end, enabled) },
                    onStartConversion = { viewModel.startConversion() },
                    onPreviewTrack = { path, name -> viewModel.playAudio(path, name) }
                )

                1 -> PresetsScreen(
                    presets = viewModel.defaultPresets,
                    onApplyPreset = { viewModel.applyPreset(it) }
                )

                2 -> FilesHistoryScreen(
                    audios = historyAudios,
                    searchQuery = searchQuery,
                    filterFormat = filterFormat,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onFilterFormatChange = { viewModel.setFilterFormat(it) },
                    onPlayAudio = { path, name -> viewModel.playAudio(path, name) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDeleteAudio = { viewModel.deleteConvertedAudio(it) }
                )

                3 -> AudioInspectorScreen(
                    track = selectedTrack
                )
            }
        }

        // Conversion Progress Dialog
        ConversionProgressDialog(
            state = conversionState,
            onDismiss = { viewModel.dismissConversionState() },
            onPlayConverted = { path, name ->
                viewModel.dismissConversionState()
                viewModel.playAudio(path, name)
            },
            onShareConverted = { path -> }
        )
    }
}
