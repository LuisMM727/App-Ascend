package com.iglesia.jovenes.presentation

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
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesia.jovenes.domain.FeaturedMission
import com.iglesia.jovenes.domain.HomeActivity
import com.iglesia.jovenes.domain.HomeData
import com.iglesia.jovenes.domain.LeaderboardEntry
import com.iglesia.jovenes.domain.UserHeader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class HomeDestination(val label: String, val icon: ImageVector) {
    Home("Inicio", Icons.Outlined.Home),
    Missions("Misiones", Icons.Outlined.TrackChanges),
    Ranking("Clasificación", Icons.Outlined.EmojiEvents),
    Profile("Perfil", Icons.Outlined.Person),
}

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onLogout: () -> Unit = {},
    onDestinationSelected: (HomeDestination) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onRetry = viewModel::refresh,
        onLogout = onLogout,
        onEnroll = viewModel::enrollInActivity,
        onCancelEnrollment = viewModel::cancelEnrollment,
        onDestinationSelected = onDestinationSelected,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onEnroll: (Int) -> Unit,
    onCancelEnrollment: (Int) -> Unit,
    onDestinationSelected: (HomeDestination) -> Unit,
) {
    Scaffold(
        containerColor = AppBackground,
        bottomBar = { HomeBottomBar(onDestinationSelected) },
    ) { padding ->
        when (state) {
            HomeUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = NeonCyan) }

            is HomeUiState.Error -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.message, color = MutedText)
                Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Reintentar")
                }
            }

            is HomeUiState.Success -> HomeContent(
                data = state.data,
                onLogout = onLogout,
                onEnroll = onEnroll,
                onCancelEnrollment = onCancelEnrollment,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun HomeContent(
    data: HomeData,
    onLogout: () -> Unit,
    onEnroll: (Int) -> Unit,
    onCancelEnrollment: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { UserHeaderCard(data.header, onLogout) }

        if (data.missions.isNotEmpty()) {
            item { SectionTitle("Misiones Activas", "Haz clic para desplegar los detalles de la misión") }
            items(data.missions, key = { it.missionId }) { mission ->
                MissionCard(mission)
            }
        } else data.featuredMission?.let { mission ->
            item { SectionTitle("Misión Destacada", "Haz clic para desplegar los detalles") }
            item { MissionCard(mission) }
        }

        item { SectionTitle("Próximas actividades", "Conecta, participa y gana XP") }
        items(data.activities, key = { it.activityId }) { activity ->
            ActivityCard(
                activity = activity,
                onEnroll = { onEnroll(activity.activityId) },
                onCancelEnrollment = { onCancelEnrollment(activity.activityId) },
            )
        }

        item { SectionTitle("Clasificación", "Líderes de esta semana") }
        item { LeaderboardCard(data.leaderboard) }
    }
}

@Composable
private fun UserHeaderCard(header: UserHeader, onLogout: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .06f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).background(
                        Brush.linearGradient(listOf(NeonCyan, ElectricPurple)),
                        CircleShape,
                    ).padding(2.dp).background(CardSurface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(header.fullName.initials(), fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        "¡Hola, ${header.fullName.substringBefore(" ")}!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("Sigue creciendo y compartiendo", color = MutedText)
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onLogout, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MutedText,
                        )
                    }
                    Text(
                        "Nivel ${header.levelOrder}",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row(Modifier.padding(top = 18.dp)) {
                Text(header.levelName, modifier = Modifier.weight(1f), color = MutedText)
                Text(
                    "${header.accumulatedPoints} / ${header.requiredPoints} XP",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                )
            }
            val safeProgress = remember(header.progress) {
                if (header.progress.isNaN() || header.progress.isInfinite()) 0f
                else header.progress.coerceIn(0f, 1f)
            }
            LinearProgressIndicator(
                progress = { safeProgress },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp).clip(CircleShape),
                color = NeonCyan,
                trackColor = Color(0xFF252A40),
            )
        }
    }
}

@Composable
private fun MissionCard(mission: FeaturedMission) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(ElectricPurple.copy(alpha = .55f), CardSurface),
                ),
            )
            .border(
                1.dp,
                ElectricPurple.copy(alpha = .35f),
                RoundedCornerShape(28.dp),
            )
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(animationSpec = tween(durationMillis = 300))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("MISIÓN DESTACADA", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = NeonCyan)
            }

            Spacer(Modifier.weight(1f))

            Text("+${mission.bonusPoints} XP", color = NeonCyan, fontWeight = FontWeight.Bold)

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                tint = NeonCyan,
            )
        }

        Text(
            mission.name,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )

        Text(
            mission.description,
            modifier = Modifier.padding(top = 8.dp),
            color = MutedText,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (mission.totalActivities > 0) {
            val missionProgress = remember(mission.completedActivities, mission.totalActivities) {
                if (mission.totalActivities <= 0) 0f
                else {
                    val raw = mission.completedActivities.toFloat() / mission.totalActivities
                    if (raw.isNaN() || raw.isInfinite()) 0f else raw.coerceIn(0f, 1f)
                }
            }
            LinearProgressIndicator(
                progress = { missionProgress },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(7.dp).clip(CircleShape),
                color = NeonCyan,
                trackColor = Color.White.copy(alpha = .1f),
            )
            Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${mission.completedActivities}/${mission.totalActivities} actividades completadas",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        // Sección Desplegable con Detalles Adicionales
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (mission.endsAt > 0) "Válido hasta: ${formatDateDateOnly(mission.endsAt)}" else "Misión de tiempo limitado",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Stars,
                        contentDescription = null,
                        tint = RewardGold,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Recompensa: +${mission.bonusPoints} XP al completar todos los objetivos",
                        color = RewardGold,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Text(
                    text = "Participa en los eventos y actividades comunitarias ligadas a esta misión para acumular experiencia y ayudar a tu comunidad.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { isExpanded = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = AppBackground,
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Ocultar Detalles", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (!isExpanded) {
            OutlinedButton(
                onClick = { isExpanded = true },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Desplegar Misión", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: HomeActivity,
    onEnroll: () -> Unit,
    onCancelEnrollment: () -> Unit,
) {
    val isEnrolled = activity.enrollmentStatus != null
    val isFull = activity.currentCapacity >= activity.maximumCapacity

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .06f)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).background(
                        ElectricPurple.copy(alpha = .15f),
                        RoundedCornerShape(14.dp),
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Place, null, tint = Color(0xFFB39CFF))
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        activity.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${activity.location} · ${formatDate(activity.date)}",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "${activity.currentCapacity}/${activity.maximumCapacity} inscritos",
                        color = if (isFull && !isEnrolled) Color(0xFFFF7A85) else RewardGold,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text("+${activity.totalPoints} XP", color = NeonCyan, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isEnrolled) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E3B2A), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = if (activity.enrollmentStatus == "COMPLETADA") "Asistió ✓ (+${activity.totalPoints} XP)" else "Inscrito ✓",
                            color = Color(0xFF6BFFB8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    if (activity.enrollmentStatus != "COMPLETADA") {
                        TextButton(onClick = onCancelEnrollment) {
                            Text("Cancelar", color = Color(0xFFFF7A85), fontSize = 12.sp)
                        }
                    }
                } else if (isFull) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF3D1C22), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text("Cupos agotados", color = Color(0xFFFF7A85), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onEnroll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text("Inscribirme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardCard(entries: List<LeaderboardEntry>) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardSurface)) {
        entries.forEachIndexed { index, entry ->
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "#${entry.position}",
                    modifier = Modifier.width(36.dp),
                    color = when (entry.position) {
                        1 -> RewardGold
                        2 -> Color(0xFFCAD0E0)
                        else -> Color(0xFFE39B72)
                    },
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(entry.fullName, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("${entry.points} XP", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
            if (index < entries.lastIndex) {
                HorizontalDivider(color = Color.White.copy(alpha = .05f))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = MutedText, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun HomeBottomBar(onSelected: (HomeDestination) -> Unit) {
    NavigationBar(containerColor = AppBackground) {
        HomeDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == HomeDestination.Home,
                onClick = { onSelected(destination) },
                icon = { Icon(destination.icon, destination.label) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan.copy(alpha = .12f),
                    unselectedIconColor = MutedText,
                    unselectedTextColor = MutedText,
                ),
            )
        }
    }
}

private fun String.initials(): String =
    trim().split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")

private fun formatDate(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("dd MMM · HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))

private fun formatDateDateOnly(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("dd MMM yyyy")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
