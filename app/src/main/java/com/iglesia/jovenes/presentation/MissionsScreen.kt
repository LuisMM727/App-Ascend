package com.iglesia.jovenes.presentation

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesia.jovenes.domain.MissionActivityTask
import com.iglesia.jovenes.domain.MissionDetailItem
import com.iglesia.jovenes.domain.MissionsScreenData
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MissionsRoute(
    viewModel: MissionsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    MissionsScreen(
        uiState = uiState,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::setFilter,
        onRetry = viewModel::loadMissions,
        onEnroll = { id, ctx, title, loc -> viewModel.enrollInActivity(id, ctx, title, loc) },
        onCancelEnrollment = viewModel::cancelEnrollment,
        modifier = modifier,
    )
}

@Composable
fun MissionsScreen(
    uiState: MissionsUiState,
    selectedFilter: MissionsFilter,
    onFilterSelected: (MissionsFilter) -> Unit,
    onRetry: () -> Unit,
    onEnroll: (Int, Context, String, String) -> Unit,
    onCancelEnrollment: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(AppBackground),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            MissionsUiState.Loading -> CircularProgressIndicator(color = NeonCyan)

            is MissionsUiState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(uiState.message, color = MutedText, textAlign = TextAlign.Center)
                Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Reintentar")
                }
            }

            is MissionsUiState.Success -> MissionsContent(
                data = uiState.data,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
                onEnroll = onEnroll,
                onCancelEnrollment = onCancelEnrollment,
            )
        }
    }
}

@Composable
private fun MissionsContent(
    data: MissionsScreenData,
    selectedFilter: MissionsFilter,
    onFilterSelected: (MissionsFilter) -> Unit,
    onEnroll: (Int, Context, String, String) -> Unit,
    onCancelEnrollment: (Int) -> Unit,
) {
    val filteredMissions = remember(data.missions, selectedFilter) {
        when (selectedFilter) {
            MissionsFilter.ALL -> data.missions
            MissionsFilter.ACTIVE -> data.missions.filter { !it.isCompleted }
            MissionsFilter.COMPLETED -> data.missions.filter { it.isCompleted }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Header de Misiones
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                        .background(ElectricPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrackChanges,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp),
                ) {
                    Text(
                        text = "Misiones y Desafíos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = "Completa objetivos comunitarios y gana XP extra",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
            }
        }

        // Pestañas de Filtro
        MissionsFilterTabRow(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
        )

        // Lista de Misiones
        if (filteredMissions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (selectedFilter) {
                        MissionsFilter.ACTIVE -> "No tienes misiones activas por el momento."
                        MissionsFilter.COMPLETED -> "Aún no has completado ninguna misión."
                        else -> "No hay misiones registradas."
                    },
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(filteredMissions, key = { it.missionId }) { mission ->
                    ExpandableMissionCard(
                        mission = mission,
                        onEnroll = onEnroll,
                        onCancelEnrollment = onCancelEnrollment,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionsFilterTabRow(
    selectedFilter: MissionsFilter,
    onFilterSelected: (MissionsFilter) -> Unit,
) {
    TabRow(
        selectedTabIndex = selectedFilter.ordinal,
        containerColor = AppBackground,
        contentColor = NeonCyan,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFilter.ordinal]),
                color = NeonCyan,
            )
        },
    ) {
        MissionsFilter.entries.forEach { filter ->
            val label = when (filter) {
                MissionsFilter.ALL -> "Todas"
                MissionsFilter.ACTIVE -> "Activas"
                MissionsFilter.COMPLETED -> "Completadas"
            }
            Tab(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = label,
                        fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                    )
                },
                selectedContentColor = NeonCyan,
                unselectedContentColor = MutedText,
            )
        }
    }
}

@Composable
private fun ExpandableMissionCard(
    mission: MissionDetailItem,
    onEnroll: (Int, Context, String, String) -> Unit,
    onCancelEnrollment: (Int) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        if (mission.isCompleted) Color(0xFF1B2A26) else ElectricPurple.copy(alpha = 0.5f),
                        CardSurface,
                    ),
                ),
            )
            .border(
                1.dp,
                if (mission.isCompleted) Color(0xFF6BFFB8).copy(alpha = 0.3f) else ElectricPurple.copy(alpha = 0.35f),
                RoundedCornerShape(24.dp),
            )
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(animationSpec = tween(durationMillis = 300))
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(
                        if (mission.isCompleted) Color(0xFF1E3B2A) else NeonCyan.copy(alpha = 0.15f),
                        CircleShape,
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = if (mission.isCompleted) "COMPLETADA ✓" else "MISIÓN ACTIVA",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = if (mission.isCompleted) Color(0xFF6BFFB8) else NeonCyan,
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "+${mission.bonusPoints} XP",
                color = RewardGold,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = NeonCyan,
            )
        }

        Text(
            text = mission.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(top = 12.dp),
        )

        Text(
            text = mission.description,
            style = MaterialTheme.typography.bodySmall,
            color = MutedText,
            modifier = Modifier.padding(top = 6.dp),
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )

        val safeProgress = remember(mission.progress) {
            if (mission.progress.isNaN() || mission.progress.isInfinite()) 0f
            else mission.progress.coerceIn(0f, 1f)
        }

        LinearProgressIndicator(
            progress = { safeProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .height(7.dp)
                .clip(CircleShape),
            color = if (mission.isCompleted) Color(0xFF6BFFB8) else NeonCyan,
            trackColor = Color.White.copy(alpha = 0.1f),
        )

        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${mission.completedActivities}/${mission.totalActivities} tareas completadas",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }

        // Desglose de Tareas al Expandir
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text(
                    text = "Tareas y Actividades Vinculadas:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                if (mission.activities.isEmpty()) {
                    Text(
                        text = "Esta misión no requiere actividades específicas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                } else {
                    mission.activities.forEach { task ->
                        TaskItemCard(
                            task = task,
                            onEnroll = { ctx, title, loc -> onEnroll(task.activityId, ctx, title, loc) },
                            onCancelEnrollment = { onCancelEnrollment(task.activityId) },
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (mission.endsAt > 0) "Vence: ${formatDateOnly(mission.endsAt)}" else "Tiempo ilimitado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }

                OutlinedButton(
                    onClick = { isExpanded = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Ocultar Detalles", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TaskItemCard(
    task: MissionActivityTask,
    onEnroll: (Context, String, String) -> Unit,
    onCancelEnrollment: () -> Unit,
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (task.isCompleted) Color(0xFF1E3B2A) else ElectricPurple.copy(alpha = 0.15f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Outlined.Check else Icons.Outlined.Event,
                        contentDescription = null,
                        tint = if (task.isCompleted) Color(0xFF6BFFB8) else ElectricPurple,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                ) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${task.location} · ${formatDateOnly(task.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                    )
                }

                Text(
                    text = "+${task.totalPoints} XP",
                    color = RewardGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    task.isCompleted -> {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E3B2A), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text("Completada ✓ (+${task.totalPoints} XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6BFFB8))
                        }
                    }

                    task.isEnrolled -> {
                        Box(
                            modifier = Modifier
                                .background(ElectricPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text("Inscrito ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        }

                        Spacer(Modifier.weight(1f))

                        TextButton(onClick = onCancelEnrollment) {
                            Text("Cancelar", color = Color(0xFFFF7A85), fontSize = 11.sp)
                        }
                    }

                    else -> {
                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = { onEnroll(context, task.title, task.location) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text("Inscribirme", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateOnly(epochMillis: Long): String =
    if (epochMillis <= 0) "" else try {
        DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(epochMillis))
    } catch (e: Exception) {
        ""
    }
