package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ConversionProgressState
import com.example.ui.theme.AudioGreen
import com.example.ui.theme.AudioGreenContainer
import com.example.ui.theme.StudioCobalt

@Composable
fun ConversionProgressDialog(
    state: ConversionProgressState,
    onDismiss: () -> Unit,
    onPlayConverted: (filePath: String, name: String) -> Unit,
    onShareConverted: (filePath: String) -> Unit
) {
    if (state is ConversionProgressState.Idle) return

    Dialog(
        onDismissRequest = {
            if (state is ConversionProgressState.Success || state is ConversionProgressState.Error) {
                onDismiss()
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is ConversionProgressState.Converting -> {
                        Text(
                            text = "Convertiendo Audio...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Waveform Animation
                        AudioWaveformAnimation()

                        Spacer(modifier = Modifier.height(20.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = StudioCobalt,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${state.progressPercent}% completado",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = StudioCobalt
                            )
                            Text(
                                text = "ETA: ~${state.estimatedSecRemaining}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = state.currentStep,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is ConversionProgressState.Success -> {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AudioGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Éxito",
                                tint = AudioGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "¡Conversión Completada!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioCobalt,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Comparison Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Tamaño Original",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatFileSize(state.originalSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = "Ahorro",
                                        tint = AudioGreen
                                    )

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Nuevo Tamaño",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatFileSize(state.newSize),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AudioGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val savingsPercent = if (state.originalSize > 0) {
                                    ((state.originalSize - state.newSize).toDouble() / state.originalSize * 100).toInt()
                                } else 0

                                val savingsText = if (savingsPercent > 0) {
                                    "✨ Ahorro de espacio: $savingsPercent% más ligero"
                                } else if (savingsPercent < 0) {
                                    " Formato PCM sin pérdida (Alta calidad)"
                                } else {
                                    " Formato convertido exitosamente"
                                }

                                Text(
                                    text = savingsText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onPlayConverted(state.outputFilePath, state.fileName)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCobalt)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reproducir")
                            }

                            OutlinedButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }

                    is ConversionProgressState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Error en la conversión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Entendido")
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun AudioWaveformAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "Waveform")
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(48.dp)
    ) {
        val heights = listOf(24.dp, 40.dp, 16.dp, 48.dp, 32.dp, 20.dp, 44.dp)
        heights.forEachIndexed { index, defaultHeight ->
            val animFraction by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 350 + (index * 80)),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BarHeight_$index"
            )

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(defaultHeight * animFraction)
                    .clip(CircleShape)
                    .background(StudioCobalt)
            )
        }
    }
}
