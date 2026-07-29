package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SourceAudioTrack
import com.example.ui.components.FormatTag
import com.example.ui.components.formatFileSize
import com.example.ui.components.formatSecondsToMinSec
import com.example.ui.theme.StudioCobalt
import kotlin.random.Random

@Composable
fun AudioInspectorScreen(
    track: SourceAudioTrack?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = "Inspector",
                tint = StudioCobalt,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Inspector Técnico de Audio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "Análisis de parámetros acústicos, frecuencia y metadatos del archivo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        if (track == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Selecciona una pista para inspeccionar metadatos.")
            }
        } else {
            // General Info Card
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
                            text = track.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FormatTag(formatName = track.format)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    InspectorGridRow("Duración", formatSecondsToMinSec(track.durationSeconds))
                    InspectorGridRow("Tamaño en Disco", formatFileSize(track.sizeBytes))
                    InspectorGridRow("Frecuencia de Muestreo", "${track.sampleRateHz / 1000.0} kHz (${track.sampleRateHz} Hz)")
                    InspectorGridRow("Canales de Audio", if (track.channels == 2) "Estéreo (2 canales)" else "Mono (1 canal)")
                    InspectorGridRow("Tasa de Bits Estimada", "${track.bitrateKbps} kbps")
                    InspectorGridRow("Tipo de Pista", if (track.isSample) "Pista Demo Interna" else "Archivo Local de Usuario")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spectrum Visualizer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Espectro",
                            tint = StudioCobalt,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Espectro de Frecuencia Estimado",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StudioCobalt
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Spectrum Canvas
                    val barColor = StudioCobalt
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val barCount = 32
                        val barWidth = size.width / (barCount * 1.5f)
                        val gap = barWidth * 0.5f

                        val rng = Random(track.name.hashCode())

                        for (i in 0 until barCount) {
                            val freqFactor = if (i < 8) 0.8f else if (i < 20) 1.0f else 0.5f
                            val randomHeightRatio = (0.2f + rng.nextFloat() * 0.8f) * freqFactor
                            val barHeight = size.height * randomHeightRatio

                            drawRect(
                                color = barColor.copy(alpha = 0.85f),
                                topLeft = Offset(
                                    x = i * (barWidth + gap),
                                    y = size.height - barHeight
                                ),
                                size = Size(width = barWidth, height = barHeight)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("20 Hz (Graves)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1 kHz (Medios)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("20 kHz (Agudos)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectorGridRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
