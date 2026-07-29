package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioFormat
import com.example.model.ConversionConfig
import com.example.model.SourceAudioTrack
import com.example.ui.components.FormatTag
import com.example.ui.components.formatFileSize
import com.example.ui.components.formatSecondsToMinSec
import com.example.ui.theme.Slate200
import com.example.ui.theme.StudioCobalt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConverterScreen(
    selectedTrack: SourceAudioTrack?,
    sampleTracks: List<SourceAudioTrack>,
    config: ConversionConfig,
    onSelectSampleTrack: (SourceAudioTrack) -> Unit,
    onCustomAudioPicked: (android.net.Uri) -> Unit,
    onSetTargetFormat: (AudioFormat) -> Unit,
    onSetBitrate: (Int) -> Unit,
    onSetSampleRate: (Int) -> Unit,
    onSetChannels: (Int) -> Unit,
    onSetVolumeGain: (Float) -> Unit,
    onSetTrimRange: (startSec: Double, endSec: Double, isEnabled: Boolean) -> Unit,
    onStartConversion: () -> Unit,
    onPreviewTrack: (filePath: String, name: String) -> Unit
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onCustomAudioPicked(it) }
    }

    var showSampleMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section 1: Source Audio Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Archivo de Origen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StudioCobalt
                    )

                    if (selectedTrack?.isSample == true) {
                        Surface(
                            shape = CircleShape,
                            color = StudioCobalt.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Pista de Ejemplo",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioCobalt,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTrack != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioCobalt.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = "Audio",
                                tint = StudioCobalt,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedTrack.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FormatTag(formatName = selectedTrack.format)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${formatFileSize(selectedTrack.sizeBytes)} • ${formatSecondsToMinSec(selectedTrack.durationSeconds)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Play Preview
                        if (selectedTrack.localFilePath != null) {
                            Surface(
                                shape = CircleShape,
                                color = StudioCobalt,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        onPreviewTrack(
                                            selectedTrack.localFilePath,
                                            selectedTrack.name
                                        )
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Previsualizar",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_file_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCobalt)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Elegir archivo",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Elegir Mi Audio")
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showSampleMenu = true },
                            modifier = Modifier.testTag("pick_sample_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = "Demos",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pistas Demo")
                        }

                        DropdownMenu(
                            expanded = showSampleMenu,
                            onDismissRequest = { showSampleMenu = false }
                        ) {
                            sampleTracks.forEach { sample ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                sample.name,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "${sample.format} • ${formatSecondsToMinSec(sample.durationSeconds)}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectSampleTrack(sample)
                                        showSampleMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Target Format Grid Choice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Formato Destino",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = StudioCobalt
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AudioFormat.entries.forEach { fmt ->
                        val isSelected = config.targetFormat == fmt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetTargetFormat(fmt) },
                            label = {
                                Text(
                                    text = fmt.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioCobalt,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("format_chip_${fmt.displayName}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = config.targetFormat.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Audio Quality & Bitrate Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Calidad",
                            tint = StudioCobalt,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Parámetros de Calidad",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StudioCobalt
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source audio detection info banner
                val maxSourceBitrate = if (selectedTrack != null && selectedTrack.bitrateKbps in 1..320) selectedTrack.bitrateKbps else 320
                val maxSourceSampleRate = if (selectedTrack != null && selectedTrack.sampleRateHz > 0) selectedTrack.sampleRateHz else 48000

                if (selectedTrack != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Audio original: ${selectedTrack.bitrateKbps} kbps • ${selectedTrack.sampleRateHz / 1000.0} kHz. Opciones superiores deshabilitadas para evitar re-muestreo innecesario.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Bitrate Control (if applicable)
                if (config.targetFormat.supportsBitrate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tasa de Bits (Bitrate):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${config.bitrateKbps} kbps",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StudioCobalt
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val bitrateOptions = listOf(64, 96, 128, 192, 256, 320)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        bitrateOptions.forEach { b ->
                            val isExceeding = b > maxSourceBitrate
                            val isSel = config.bitrateKbps == b
                            FilterChip(
                                selected = isSel,
                                enabled = !isExceeding,
                                onClick = { onSetBitrate(b) },
                                label = {
                                    Text(
                                        text = if (isExceeding) "$b kbps 🚫" else "$b kbps",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioCobalt,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Sample Rate Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Frecuencia de Muestreo:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${config.sampleRateHz / 1000.0} kHz",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StudioCobalt
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val sampleRateOptions = listOf(22050, 32000, 44100, 48000)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleRateOptions.forEach { sr ->
                        val isExceeding = sr > maxSourceSampleRate
                        val isSel = config.sampleRateHz == sr
                        FilterChip(
                            selected = isSel,
                            enabled = !isExceeding,
                            onClick = { onSetSampleRate(sr) },
                            label = {
                                Text(
                                    text = if (isExceeding) "${sr / 1000.0} kHz 🚫" else "${sr / 1000.0} kHz",
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioCobalt,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Channels Toggle (Stereo / Mono)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Canales de Audio:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = config.channels == 2,
                            onClick = { onSetChannels(2) },
                            label = { Text("Estéreo (2 ch)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioCobalt,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = config.channels == 1,
                            onClick = { onSetChannels(1) },
                            label = { Text("Mono (1 ch)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioCobalt,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volume Gain Booster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volumen",
                            tint = StudioCobalt,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ajuste de Ganancia:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    val gainLabel = if (config.volumeGainDb > 0) {
                        "+${String.format("%.1f", config.volumeGainDb)} dB"
                    } else {
                        "${String.format("%.1f", config.volumeGainDb)} dB"
                    }

                    Text(
                        text = gainLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StudioCobalt
                    )
                }

                Slider(
                    value = config.volumeGainDb,
                    onValueChange = { onSetVolumeGain(it) },
                    valueRange = -6f..6f,
                    steps = 11,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = StudioCobalt,
                        activeTrackColor = StudioCobalt
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Audio Trimmer Tool
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Recortar",
                            tint = StudioCobalt,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4. Recortar Audio (Opcional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StudioCobalt
                        )
                    }

                    Switch(
                        checked = config.isTrimEnabled,
                        onCheckedChange = { isEnabled ->
                            val duration = selectedTrack?.durationSeconds ?: 10.0
                            onSetTrimRange(
                                config.trimStartSec,
                                if (config.trimEndSec > 0) config.trimEndSec else duration,
                                isEnabled
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = StudioCobalt)
                    )
                }

                AnimatedVisibility(visible = config.isTrimEnabled) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        val maxDuration = (selectedTrack?.durationSeconds ?: 10.0).toFloat().coerceAtLeast(1f)
                        val rangeStart = config.trimStartSec.toFloat().coerceIn(0f, maxDuration)
                        val rangeEnd = if (config.trimEndSec > 0) {
                            config.trimEndSec.toFloat().coerceIn(rangeStart, maxDuration)
                        } else maxDuration

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Inicio: ${formatSecondsToMinSec(rangeStart.toDouble())}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = StudioCobalt
                            )
                            Text(
                                text = "Fin: ${formatSecondsToMinSec(rangeEnd.toDouble())}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = StudioCobalt
                            )
                        }

                        RangeSlider(
                            value = rangeStart..rangeEnd,
                            onValueChange = { range ->
                                onSetTrimRange(
                                    range.start.toDouble(),
                                    range.endInclusive.toDouble(),
                                    true
                                )
                            },
                            valueRange = 0f..maxDuration,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = StudioCobalt,
                                activeTrackColor = StudioCobalt
                            )
                        )

                        Text(
                            text = "Duración resultante: ${formatSecondsToMinSec((rangeEnd - rangeStart).toDouble())}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Convert Main Action Button
        Button(
            onClick = onStartConversion,
            enabled = selectedTrack != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("convert_audio_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCobalt)
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Convertir",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "CONVERTIR A ${config.targetFormat.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
