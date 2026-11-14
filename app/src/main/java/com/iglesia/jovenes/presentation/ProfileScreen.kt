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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iglesia.jovenes.domain.ProfileBadgeItem
import com.iglesia.jovenes.domain.UserProfileData

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showEditDialog by viewModel.showEditDialog.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        showEditDialog = showEditDialog,
        isSaving = isSaving,
        errorMessage = errorMessage,
        onRetry = viewModel::loadProfile,
        onOpenEdit = viewModel::openEditDialog,
        onCloseEdit = viewModel::closeEditDialog,
        onSaveDetails = viewModel::updateProfileDetails,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    showEditDialog: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onOpenEdit: () -> Unit,
    onCloseEdit: () -> Unit,
    onSaveDetails: (church: String, age: Int, phone: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(AppBackground),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            ProfileUiState.Loading -> CircularProgressIndicator(color = NeonCyan)

            is ProfileUiState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(uiState.message, color = MutedText, textAlign = TextAlign.Center)
                Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Reintentar")
                }
            }

            is ProfileUiState.Success -> ProfileContent(
                data = uiState.profileData,
                onOpenEdit = onOpenEdit,
            )
        }
    }

    if (showEditDialog && uiState is ProfileUiState.Success) {
        EditProfileDialog(
            currentChurch = uiState.profileData.church ?: "",
            currentAge = uiState.profileData.age ?: 0,
            currentPhone = uiState.profileData.phone ?: "",
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = onCloseEdit,
            onSave = onSaveDetails,
        )
    }
}

@Composable
private fun ProfileContent(
    data: UserProfileData,
    onOpenEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header Card de Usuario
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.linearGradient(listOf(NeonCyan, ElectricPurple)),
                                CircleShape,
                            )
                            .padding(3.dp)
                            .background(CardSurface, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = data.fullName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                    ) {
                        Text(
                            text = data.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                        Text(
                            text = data.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText,
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .background(ElectricPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Joven Ascend",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.06f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.levelName, color = MutedText, modifier = Modifier.weight(1f))
                    Text(
                        text = "Nivel ${data.levelOrder}",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                    )
                }

                val safeProgress = remember(data.progress) {
                    if (data.progress.isNaN() || data.progress.isInfinite()) 0f
                    else data.progress.coerceIn(0f, 1f)
                }

                LinearProgressIndicator(
                    progress = { safeProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = NeonCyan,
                    trackColor = Color(0xFF252A40),
                )

                Text(
                    text = "${data.accumulatedPoints} / ${data.requiredPoints} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        // Fila de Estadísticas Generales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileStatCard(
                title = "Puntos XP",
                value = "${data.accumulatedPoints}",
                icon = Icons.Outlined.Stars,
                accentColor = RewardGold,
                modifier = Modifier.weight(1f),
            )
            ProfileStatCard(
                title = "Actividades",
                value = "${data.completedActivitiesCount}",
                icon = Icons.Outlined.EventAvailable,
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f),
            )
        }

        // Sección de Insignias y Logros
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Mis Insignias y Medallas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            if (data.badges.isEmpty()) {
                Text("Aún no hay insignias registradas.", color = MutedText, fontSize = 13.sp)
            } else {
                data.badges.forEach { badge ->
                    BadgeCard(badge = badge)
                }
            }
        }

        // Sección de Información Personal
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onOpenEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = NeonCyan)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = Color.White.copy(alpha = 0.06f))

                ProfileInfoRow(
                    icon = Icons.Outlined.Church,
                    label = "Iglesia",
                    value = data.church ?: "Sin especificar",
                )

                ProfileInfoRow(
                    icon = Icons.Outlined.Person,
                    label = "Edad",
                    value = data.age?.let { "$it años" } ?: "Sin especificar",
                )

                ProfileInfoRow(
                    icon = Icons.Outlined.Phone,
                    label = "Teléfono",
                    value = data.phone ?: "Sin especificar",
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(title, style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
    }
}

@Composable
private fun BadgeCard(badge: ProfileBadgeItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) CardSurface else CardSurface.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) NeonCyan.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.04f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (badge.isUnlocked) RewardGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (badge.isUnlocked) Icons.Outlined.WorkspacePremium else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) RewardGold else MutedText,
                    modifier = Modifier.size(26.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    text = badge.name,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) Color.White else MutedText,
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                )
            }

            if (badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E3B2A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = Color(0xFF6BFFB8), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ganada", fontSize = 10.sp, color = Color(0xFF6BFFB8), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = "${badge.conditionValue} XP",
                    fontSize = 11.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MutedText, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedText)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentChurch: String,
    currentAge: Int,
    currentPhone: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (church: String, age: Int, phone: String) -> Unit,
) {
    var church by remember { mutableStateOf(currentChurch) }
    var ageText by remember { mutableStateOf(if (currentAge > 0) currentAge.toString() else "") }
    var phone by remember { mutableStateOf(currentPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = { Text("Editar Datos Personales", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3B1E28), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    ) {
                        Text(error, color = Color(0xFFFF8A8A), fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }

                OutlinedTextField(
                    value = church,
                    onValueChange = { church = it },
                    label = { Text("Iglesia") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                )

                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                    label = { Text("Edad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAge = ageText.toIntOrNull() ?: 0
                    if (church.isNotBlank() && parsedAge > 0) {
                        onSave(church, parsedAge, phone)
                    }
                },
                enabled = !isSaving && church.isNotBlank() && (ageText.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = AppBackground),
            ) {
                if (isSaving) CircularProgressIndicator(color = AppBackground, modifier = Modifier.size(18.dp))
                else Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MutedText) }
        },
    )
}
