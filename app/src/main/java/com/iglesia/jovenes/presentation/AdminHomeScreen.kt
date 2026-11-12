package com.iglesia.jovenes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.OutlinedFlag
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesia.jovenes.domain.AdminActivityItem
import com.iglesia.jovenes.domain.AdminLevelItem
import com.iglesia.jovenes.domain.AdminMissionItem
import com.iglesia.jovenes.domain.AdminStats
import com.iglesia.jovenes.domain.AdminUserItem
import com.iglesia.jovenes.domain.AuthUser
import com.iglesia.jovenes.domain.EnrolledUserItem

@Composable
fun AdminHomeScreen(
    adminUser: AuthUser,
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val missions by viewModel.missions.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val levels by viewModel.levels.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val showCreateMission by viewModel.showCreateMissionDialog.collectAsStateWithLifecycle()
    val showCreateActivity by viewModel.showCreateActivityDialog.collectAsStateWithLifecycle()
    val showEnrolledUsers by viewModel.showEnrolledUsersDialog.collectAsStateWithLifecycle()
    val selectedActivity by viewModel.selectedActivity.collectAsStateWithLifecycle()
    val enrolledUsers by viewModel.enrolledUsers.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            AdminTopBar(
                adminUser = adminUser,
                onLogout = onLogout,
            )
        },
        floatingActionButton = {
            if (selectedTab == AdminTab.MISSIONS) {
                FloatingActionButton(
                    onClick = { viewModel.setShowCreateMissionDialog(true) },
                    containerColor = ElectricPurple,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Nueva Misión")
                }
            } else if (selectedTab == AdminTab.ACTIVITIES) {
                FloatingActionButton(
                    onClick = { viewModel.setShowCreateActivityDialog(true) },
                    containerColor = NeonCyan,
                    contentColor = AppBackground,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Nueva Actividad")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Tab Bar Superior
            AdminTabsRow(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else {
                when (selectedTab) {
                    AdminTab.OVERVIEW -> OverviewTabContent(
                        stats = stats,
                        onNavigateTab = { viewModel.selectTab(it) },
                    )

                    AdminTab.USERS -> UsersTabContent(
                        users = users,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onToggleUserStatus = { id, status -> viewModel.toggleUserStatus(id, status) },
                    )

                    AdminTab.MISSIONS -> MissionsTabContent(
                        missions = missions,
                        onToggleMissionStatus = { id, status -> viewModel.toggleMissionStatus(id, status) },
                    )

                    AdminTab.ACTIVITIES -> ActivitiesTabContent(
                        activities = activities,
                        onToggleActivityStatus = { id, status -> viewModel.toggleActivityStatus(id, status) },
                        onViewEnrolledUsers = { viewModel.openEnrolledUsersDialog(it) },
                    )

                    AdminTab.SETTINGS -> SettingsTabContent(
                        levels = levels,
                        onUpdateLevelPoints = { id, points -> viewModel.updateLevelPoints(id, points) },
                    )
                }
            }
        }
    }

    // Diálogos de Creación y Ver Inscritos
    if (showCreateMission) {
        CreateMissionDialog(
            onDismiss = { viewModel.setShowCreateMissionDialog(false) },
            onCreate = { name, desc, points -> viewModel.createMission(name, desc, points) },
        )
    }

    if (showCreateActivity) {
        CreateActivityDialog(
            onDismiss = { viewModel.setShowCreateActivityDialog(false) },
            onCreate = { title, desc, loc, cap, baseP, bonusP ->
                viewModel.createActivity(title, desc, loc, cap, baseP, bonusP)
            },
        )
    }

    if (showEnrolledUsers && selectedActivity != null) {
        EnrolledUsersDialog(
            activity = selectedActivity!!,
            enrolledUsers = enrolledUsers,
            onDismiss = { viewModel.setShowEnrolledUsersDialog(false) },
            onMarkAttendance = { userId ->
                viewModel.markAttendance(userId, selectedActivity!!.id, selectedActivity!!.totalPoints)
            },
        )
    }
}

@Composable
private fun AdminTopBar(
    adminUser: AuthUser,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(ElectricPurple.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AdminPanelSettings,
                contentDescription = null,
                tint = NeonCyan,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = "Ascend Admin",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = adminUser.fullName,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }

        IconButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                contentDescription = "Cerrar Sesión",
                tint = MutedText,
            )
        }
    }
}

@Composable
private fun AdminTabsRow(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = AppBackground,
        contentColor = NeonCyan,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = NeonCyan,
            )
        },
    ) {
        AdminTab.entries.forEach { tab ->
            val (label, icon) = when (tab) {
                AdminTab.OVERVIEW -> "Resumen" to Icons.Outlined.Dashboard
                AdminTab.USERS -> "Usuarios" to Icons.Outlined.Group
                AdminTab.MISSIONS -> "Misiones" to Icons.Outlined.TrackChanges
                AdminTab.ACTIVITIES -> "Actividades" to Icons.Outlined.Event
                AdminTab.SETTINGS -> "Ajustes" to Icons.Outlined.Settings
            }
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(label, fontSize = 13.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) },
                selectedContentColor = NeonCyan,
                unselectedContentColor = MutedText,
            )
        }
    }
}

@Composable
private fun OverviewTabContent(
    stats: AdminStats?,
    onNavigateTab: (AdminTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Métricas Generales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                title = "Jóvenes",
                value = "${stats?.totalUsers ?: 0}",
                subtitle = "Registrados",
                icon = Icons.Outlined.Group,
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Misiones",
                value = "${stats?.activeMissions ?: 0}",
                subtitle = "Activas",
                icon = Icons.Outlined.TrackChanges,
                accentColor = ElectricPurple,
                modifier = Modifier.weight(1f),
            )
        }

        StatCard(
            title = "Próximas Actividades",
            value = "${stats?.upcomingActivities ?: 0}",
            subtitle = "Eventos programados comunitarios",
            icon = Icons.Outlined.Event,
            accentColor = RewardGold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Text("Acciones Rápidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { onNavigateTab(AdminTab.USERS) },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.Group, contentDescription = null, tint = NeonCyan)
                Spacer(Modifier.width(8.dp))
                Text("Ver Jóvenes", color = Color.White)
            }

            Button(
                onClick = { onNavigateTab(AdminTab.MISSIONS) },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = ElectricPurple)
                Spacer(Modifier.width(8.dp))
                Text("Misiones", color = Color.White)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = accentColor)
            }
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 12.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
    }
}

@Composable
private fun UsersTabContent(
    users: List<AdminUserItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleUserStatus: (Int, String) -> Unit,
) {
    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    (it.church?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar por nombre, correo o iglesia...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = MutedText) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            items(filteredUsers, key = { it.id }) { user ->
                UserAdminCard(user = user, onToggleStatus = { onToggleUserStatus(user.id, user.status) })
            }
        }
    }
}

@Composable
private fun UserAdminCard(
    user: AdminUserItem,
    onToggleStatus: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(
                        if (user.isActive) NeonCyan.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                        CircleShape,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = user.name.take(1) + user.lastName.take(1),
                        fontWeight = FontWeight.Bold,
                        color = if (user.isActive) NeonCyan else Color.Red,
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.fullName, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (user.isActive) Color(0xFF1E3B2A) else Color(0xFF3D1C22),
                                    RoundedCornerShape(6.dp),
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = user.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user.isActive) Color(0xFF6BFFB8) else Color(0xFFFF7A85),
                            )
                        }
                    }
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MutedText)
                }

                Text("${user.points} XP", fontWeight = FontWeight.Bold, color = RewardGold)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Church, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                Text(
                    text = user.church ?: "Sin iglesia especificada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    modifier = Modifier.padding(start = 4.dp).weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                user.age?.let { age ->
                    Text("Edad: $age", style = MaterialTheme.typography.bodySmall, color = MutedText)
                }

                Spacer(Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (user.isActive) Color(0xFFFF6B6B) else NeonCyan,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(if (user.isActive) "Desactivar" else "Activar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MissionsTabContent(
    missions: List<AdminMissionItem>,
    onToggleMissionStatus: (Int, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        items(missions, key = { it.id }) { mission ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(mission.name, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        Text("+${mission.bonusPoints} XP", fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                    Text(mission.description, style = MaterialTheme.typography.bodySmall, color = MutedText, modifier = Modifier.padding(top = 6.dp))
                    Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Estado: ${mission.status}", style = MaterialTheme.typography.labelSmall, color = if (mission.isActive) NeonCyan else MutedText)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { onToggleMissionStatus(mission.id, mission.status) }) {
                            Text(if (mission.isActive) "Desactivar" else "Activar", color = if (mission.isActive) Color(0xFFFF6B6B) else NeonCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitiesTabContent(
    activities: List<AdminActivityItem>,
    onToggleActivityStatus: (Int, String) -> Unit,
    onViewEnrolledUsers: (AdminActivityItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        items(activities, key = { it.id }) { activity ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(activity.title, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        Text("+${activity.totalPoints} XP", fontWeight = FontWeight.Bold, color = RewardGold)
                    }
                    Text(activity.description, style = MaterialTheme.typography.bodySmall, color = MutedText, modifier = Modifier.padding(top = 4.dp))
                    Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                        Text(activity.location, style = MaterialTheme.typography.bodySmall, color = MutedText, modifier = Modifier.padding(start = 4.dp))
                        Spacer(Modifier.weight(1f))
                        Text("${activity.currentCapacity}/${activity.maxCapacity} cupos", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    }
                    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { onViewEnrolledUsers(activity) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        ) {
                            Text("Inscritos (${activity.currentCapacity})", fontSize = 12.sp, color = NeonCyan)
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { onToggleActivityStatus(activity.id, activity.status) }) {
                            Text(if (activity.isActive) "Desactivar" else "Activar", color = if (activity.isActive) Color(0xFFFF6B6B) else NeonCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnrolledUsersDialog(
    activity: AdminActivityItem,
    enrolledUsers: List<EnrolledUserItem>,
    onDismiss: () -> Unit,
    onMarkAttendance: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Column {
                Text("Jóvenes Inscritos", color = Color.White, fontWeight = FontWeight.Bold)
                Text(activity.title, color = MutedText, style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            if (enrolledUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Aún no hay jóvenes inscritos a esta actividad.", color = MutedText, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(enrolledUsers, key = { it.userId }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppBackground, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(user.email, color = MutedText, fontSize = 11.sp)
                            }

                            if (user.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF1E3B2A), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text("Asistió ✓ (+${user.earnedPoints} XP)", color = Color(0xFF6BFFB8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onMarkAttendance(user.userId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = AppBackground),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Asistencia", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
            ) {
                Text("Cerrar")
            }
        },
    )
}

@Composable
private fun SettingsTabContent(
    levels: List<AdminLevelItem>,
    onUpdateLevelPoints: (Int, Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp),
    ) {
        item {
            Text("Requerimientos de Niveles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
        items(levels, key = { it.id }) { level ->
            var pointsText by remember(level.requiredPoints) { mutableStateOf(level.requiredPoints.toString()) }
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nivel ${level.order}: ${level.name}", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(level.description, style = MaterialTheme.typography.bodySmall, color = MutedText)
                    }

                    OutlinedTextField(
                        value = pointsText,
                        onValueChange = { pointsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Puntos XP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(110.dp),
                        shape = RoundedCornerShape(12.dp),
                    )

                    IconButton(
                        onClick = {
                            val p = pointsText.toIntOrNull() ?: 0
                            if (p > 0) onUpdateLevelPoints(level.id, p)
                        },
                    ) {
                        Icon(Icons.Outlined.OutlinedFlag, contentDescription = "Guardar", tint = NeonCyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateMissionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bonusPointsText by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = { Text("Nueva Misión", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la misión") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                )
                OutlinedTextField(
                    value = bonusPointsText,
                    onValueChange = { bonusPointsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Puntos Bonus (XP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pts = bonusPointsText.toIntOrNull() ?: 0
                    if (name.isNotBlank() && pts > 0) onCreate(name, description, pts)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MutedText) }
        },
    )
}

@Composable
private fun CreateActivityDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Int, Int, Int) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("30") }
    var basePointsText by remember { mutableStateOf("100") }
    var bonusPointsText by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = { Text("Nueva Actividad", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lugar") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it.filter { c -> c.isDigit() } },
                    label = { Text("Cupo Máximo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = basePointsText,
                    onValueChange = { basePointsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Puntos Base") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityText.toIntOrNull() ?: 0
                    val base = basePointsText.toIntOrNull() ?: 0
                    val bonus = bonusPointsText.toIntOrNull() ?: 0
                    if (title.isNotBlank() && location.isNotBlank() && cap > 0) {
                        onCreate(title, description, location, cap, base, bonus)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = AppBackground),
            ) { Text("Crear", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MutedText) }
        },
    )
}
