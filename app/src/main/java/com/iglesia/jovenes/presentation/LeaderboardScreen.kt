package com.iglesia.jovenes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesia.jovenes.domain.LeaderboardEntryItem
import com.iglesia.jovenes.domain.LeaderboardScreenData

@Composable
fun LeaderboardRoute(
    viewModel: LeaderboardViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LeaderboardScreen(
        uiState = uiState,
        onRetry = viewModel::loadLeaderboard,
        modifier = modifier,
    )
}

@Composable
fun LeaderboardScreen(
    uiState: LeaderboardUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(AppBackground),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            LeaderboardUiState.Loading -> CircularProgressIndicator(color = NeonCyan)

            is LeaderboardUiState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(uiState.message, color = MutedText, textAlign = TextAlign.Center)
                Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Reintentar")
                }
            }

            is LeaderboardUiState.Success -> LeaderboardContent(
                data = uiState.data,
            )
        }
    }
}

@Composable
private fun LeaderboardContent(
    data: LeaderboardScreenData,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header de Clasificación
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(RewardGold.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = RewardGold,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp),
                ) {
                    Text(
                        text = "Tabla de Clasificación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = "Líderes destacados de la comunidad Ascend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
            }
        }

        // Tarjeta de Posición del Usuario Actual
        data.currentUserEntry?.let { userEntry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricPurple.copy(alpha = 0.2f), CardSurface),
                            ),
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NeonCyan.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "#${userEntry.position}",
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan,
                            fontSize = 16.sp,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            text = "Tu Puesto Actual",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = userEntry.fullName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                        )
                    }

                    Text(
                        text = "${userEntry.points} XP",
                        color = RewardGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                    )
                }
            }
        }

        // Podio del Top 3
        if (data.top3.isNotEmpty()) {
            Text(
                text = "Podio de Líderes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            PodiumView(top3 = data.top3)
        }

        // Lista Completa de Puestos #4 en adelante
        if (data.fullList.isNotEmpty()) {
            Text(
                text = "Ranking General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
            ) {
                Column {
                    data.fullList.forEachIndexed { index, entry ->
                        LeaderboardRowItem(entry = entry)
                        if (index < data.fullList.lastIndex) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PodiumView(top3: List<LeaderboardEntryItem>) {
    val first = top3.firstOrNull()
    val second = top3.getOrNull(1)
    val third = top3.getOrNull(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        // Puesto #2 (Plata - Izquierda)
        PodiumColumn(
            entry = second,
            rank = 2,
            height = 140.dp,
            accentColor = Color(0xFFCAD0E0),
            modifier = Modifier.weight(1f),
        )

        // Puesto #1 (Oro - Centro Elevado)
        PodiumColumn(
            entry = first,
            rank = 1,
            height = 175.dp,
            accentColor = RewardGold,
            modifier = Modifier.weight(1f),
        )

        // Puesto #3 (Bronce - Derecha)
        PodiumColumn(
            entry = third,
            rank = 3,
            height = 120.dp,
            accentColor = Color(0xFFE39B72),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntryItem?,
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (entry != null) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.fullName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 18.sp,
                )
            }

            Text(
                text = entry.fullName.substringBefore(" "),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )

            Text(
                text = "${entry.points} XP",
                fontSize = 11.sp,
                color = accentColor,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Spacer(Modifier.height(46.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Bloque del Podio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.35f), CardSurface),
                    ),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                )
                .border(
                    1.dp,
                    accentColor.copy(alpha = 0.4f),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = accentColor,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRowItem(entry: LeaderboardEntryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (entry.isCurrentUser) NeonCyan.copy(alpha = 0.1f) else Color.Transparent)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#${entry.position}",
            modifier = Modifier.width(36.dp),
            fontWeight = FontWeight.ExtraBold,
            color = MutedText,
            fontSize = 14.sp,
        )

        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    if (entry.isCurrentUser) NeonCyan.copy(alpha = 0.2f) else ElectricPurple.copy(alpha = 0.15f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.fullName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = if (entry.isCurrentUser) NeonCyan else Color.White,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = entry.fullName,
                fontWeight = FontWeight.Bold,
                color = if (entry.isCurrentUser) NeonCyan else Color.White,
                fontSize = 14.sp,
            )
            Text(
                text = entry.church ?: "Ascend",
                style = MaterialTheme.typography.labelSmall,
                color = MutedText,
            )
        }

        Text(
            text = "${entry.points} XP",
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            fontSize = 14.sp,
        )
    }
}
