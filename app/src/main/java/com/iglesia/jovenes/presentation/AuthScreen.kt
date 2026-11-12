package com.iglesia.jovenes.presentation

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
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iglesia.jovenes.domain.AuthUser

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    mode: AuthMode,
    isLoading: Boolean,
    errorMessage: String?,
) {
    Scaffold(
        containerColor = AppBackground,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Header Logo / Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(listOf(ElectricPurple, NeonCyan)),
                            CircleShape,
                        )
                        .padding(3.dp)
                        .background(CardSurface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Church,
                        contentDescription = "Ascend",
                        tint = NeonCyan,
                        modifier = Modifier.size(44.dp),
                    )
                }

                Text(
                    text = "Ascend",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 16.dp),
                )

                Text(
                    text = if (mode == AuthMode.LOGIN) "Bienvenido de nuevo" else "Crea tu cuenta de Joven",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                )

                // Mensaje de Error
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFF3B1E28), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFFF8A8A),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                if (mode == AuthMode.LOGIN) {
                    LoginContent(
                        isLoading = isLoading,
                        onLogin = { email, password -> viewModel.login(email, password) },
                        onSwitchToRegister = { viewModel.setMode(AuthMode.REGISTER) },
                    )
                } else {
                    RegisterContent(
                        isLoading = isLoading,
                        onRegister = { name, lastName, email, password ->
                            viewModel.register(name, lastName, email, password)
                        },
                        onSwitchToLogin = { viewModel.setMode(AuthMode.LOGIN) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginContent(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onSwitchToRegister: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo Electrónico",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
        )

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            leadingIcon = Icons.Outlined.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onPasswordToggle = { isPasswordVisible = !isPasswordVisible },
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricPurple,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("¿No tienes cuenta?", color = MutedText, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSwitchToRegister) {
                Text("Regístrate aquí", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RegisterContent(
    isLoading: Boolean,
    onRegister: (String, String, String, String) -> Unit,
    onSwitchToLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Banner aclaratorio de Administrador
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface, RoundedCornerShape(12.dp))
                .border(1.dp, ElectricPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "El registro es para miembros jóvenes. Los administradores deben iniciar sesión directamente.",
                    color = MutedText,
                    fontSize = 12.sp,
                )
            }
        }

        CustomTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre",
            leadingIcon = Icons.Outlined.Person,
        )

        CustomTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = "Apellido",
            leadingIcon = Icons.Outlined.Person,
        )

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo Electrónico",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
        )

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña (mínimo 6 caracteres)",
            leadingIcon = Icons.Outlined.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onPasswordToggle = { isPasswordVisible = !isPasswordVisible },
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onRegister(name, lastName, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isLoading && name.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.length >= 6,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = AppBackground,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = AppBackground, modifier = Modifier.size(24.dp))
            } else {
                Text("Crear Cuenta y Continuar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("¿Ya tienes una cuenta?", color = MutedText, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSwitchToLogin) {
                Text("Inicia sesión", color = ElectricPurple, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    user: AuthUser,
    isLoading: Boolean,
    errorMessage: String?,
    onCompleteProfile: (church: String, age: Int, phone: String) -> Unit,
) {
    var church by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AppBackground,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Header Onboarding
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(ElectricPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = "Datos",
                        tint = NeonCyan,
                        modifier = Modifier.size(40.dp),
                    )
                }

                Text(
                    text = "¡Hola, ${user.name}!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 16.dp),
                )

                Text(
                    text = "Completa tus datos por única vez para personalizar tu experiencia en la iglesia.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )

                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFF3B1E28), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFFF8A8A),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CustomTextField(
                        value = church,
                        onValueChange = { church = it },
                        label = "¿A qué Iglesia perteneces?",
                        leadingIcon = Icons.Outlined.Church,
                    )

                    CustomTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { char -> char.isDigit() } },
                        label = "Edad",
                        leadingIcon = Icons.Outlined.Person,
                        keyboardType = KeyboardType.Number,
                    )

                    CustomTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Teléfono / WhatsApp (opcional)",
                        leadingIcon = Icons.Outlined.Phone,
                        keyboardType = KeyboardType.Phone,
                    )

                    Spacer(Modifier.height(12.dp))

                    val parsedAge = ageText.toIntOrNull() ?: 0

                    Button(
                        onClick = { onCompleteProfile(church, parsedAge, phone) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !isLoading && church.isNotBlank() && parsedAge in 5..120,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = AppBackground,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = AppBackground, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Finalizar Registro y Entrar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = MutedText) },
        trailingIcon = if (isPassword && onPasswordToggle != null) {
            {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = MutedText,
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardSurface,
            unfocusedContainerColor = CardSurface,
            disabledContainerColor = CardSurface,
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
            focusedLabelColor = NeonCyan,
            unfocusedLabelColor = MutedText,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
    )
}
