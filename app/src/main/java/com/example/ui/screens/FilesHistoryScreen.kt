package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ConvertedAudioEntity
import com.example.ui.components.FormatTag
import com.example.ui.components.formatFileSize
import com.example.ui.components.formatSecondsToMinSec
import com.example.ui.theme.AudioAmber
import com.example.ui.theme.AudioGreen
import com.example.ui.theme.StudioCobalt
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilesHistoryScreen(
    audios: List<ConvertedAudioEntity>,
    searchQuery: String,
    filterFormat: String?,
    onSearchQueryChange: (String) -> Unit,
    onFilterFormatChange: (String?) -> Unit,
    onPlayAudio: (filePath: String, name: String) -> Unit,
    onToggleFavorite: (ConvertedAudioEntity) -> Unit,
    onDeleteAudio: (ConvertedAudioEntity) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Buscar en archivos convertidos...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = StudioCobalt
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Format Filter Chips
        val filterOptions = listOf("MP3", "WAV", "AAC", "M4A", "FLAC", "OGG")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = filterFormat == null,
                onClick = { onFilterFormatChange(null) },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StudioCobalt,
                    selectedLabelColor = Color.White
                )
            )
            filterOptions.forEach { fmt ->
                FilterChip(
                    selected = filterFormat.equals(fmt, ignoreCase = true),
                    onClick = { onFilterFormatChange(if (filterFormat == fmt) null else fmt) },
                    label = { Text(fmt) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioCobalt,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (audios.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = "Vacío",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay archivos convertidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tus audios procesados aparecerán aquí para reproducir o compartir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(audios, key = { it.id }) { item ->
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
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FormatTag(formatName = item.targetFormat)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.fileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(onClick = { onToggleFavorite(item) }) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorito",
                                        tint = if (item.isFavorite) AudioAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                                Text(
                                    text = "$dateStr • ${formatSecondsToMinSec(item.durationSeconds)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "${formatFileSize(item.originalSizeBytes)} ➔ ${formatFileSize(item.fileSizeBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AudioGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = StudioCobalt,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    IconButton(onClick = { onPlayAudio(item.outputFilePath, item.fileName) }) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Reproducir",
                                            tint = Color.White
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = { shareAudioFile(context, item.outputFilePath, item.fileName) }) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Compartir",
                                            tint = StudioCobalt
                                        )
                                    }

                                    IconButton(onClick = { onDeleteAudio(item) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareAudioFile(context: Context, filePath: String, name: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir $name"))
    } catch (_: Exception) { }
}
