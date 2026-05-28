package com.joel.proyecto2026.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.joel.proyecto2026.R
import com.joel.proyecto2026.repository.AuthRepository
import com.joel.proyecto2026.models.LoginCredentials
import com.joel.proyecto2026.ui.theme.PrimaryLight
import com.joel.proyecto2026.ui.theme.PrimaryLight2

@Composable
fun RegisterScreen(
	authRepository: AuthRepository,
	onBackToLogin: () -> Unit = {},
	onRegisterSuccess: (String) -> Unit = {}
) {
	var fullName by remember { mutableStateOf("") }
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var confirmPassword by remember { mutableStateOf("") }
	var acceptTerms by remember { mutableStateOf(false) }
	var passwordVisible by remember { mutableStateOf(false) }
	var isLoading by remember { mutableStateOf(false) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var isRegistered by remember { mutableStateOf(false) }

	if (isRegistered) {
		LaunchedEffect(Unit) { onRegisterSuccess(email.trim()) }
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				Brush.verticalGradient(
					colors = listOf(
						Color(0xFF030A1D),
						Color(0xFF0A1A43),
						Color(0xFF040C23)
					)
				)
			),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.align(Alignment.TopStart)
				.offset(x = (-48).dp, y = (-24).dp)
				.size(240.dp)
				.background(
					brush = Brush.radialGradient(
						colors = listOf(PrimaryLight.copy(alpha = 0.22f), Color.Transparent)
					),
					shape = CircleShape
				)
		)

		Box(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.offset(y = 90.dp)
				.size(280.dp)
				.background(
					brush = Brush.radialGradient(
						colors = listOf(PrimaryLight.copy(alpha = 0.10f), Color.Transparent)
					),
					shape = CircleShape
				)
		)

		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(modifier = Modifier.height(18.dp))
			Image(
				painter = painterResource(id = R.drawable.logo),
				contentDescription = "Logo",
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.size(88.dp)
					.background(
						brush = Brush.verticalGradient(listOf(PrimaryLight, PrimaryLight2)),
						shape = RoundedCornerShape(20.dp)
					)
					.then(Modifier)
			)

			Spacer(modifier = Modifier.height(12.dp))
			Text(
				"TechStock",
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.primary
			)
			Text(
				"Crear nueva cuenta",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.height(18.dp))

			Card(
				modifier = Modifier
					.fillMaxWidth(0.9f)
					.wrapContentHeight(),
				elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
				),
				shape = RoundedCornerShape(16.dp)
			) {
				Column(modifier = Modifier.padding(20.dp)) {
					Text("Registro", style = MaterialTheme.typography.titleLarge)
					Spacer(modifier = Modifier.height(12.dp))

					OutlinedTextField(
						value = fullName,
						onValueChange = {
							fullName = it
							errorMessage = null
						},
						label = { Text("Nombre completo") },
						leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)

					Spacer(modifier = Modifier.height(8.dp))

					OutlinedTextField(
						value = email,
						onValueChange = {
							email = it
							errorMessage = null
						},
						label = { Text("Correo Electrónico") },
						leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth()
					)

					Spacer(modifier = Modifier.height(8.dp))

					OutlinedTextField(
						value = password,
						onValueChange = {
							password = it
							errorMessage = null
						},
						label = { Text("Contraseña") },
						leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth(),
						visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
						trailingIcon = {
							IconButton(onClick = { passwordVisible = !passwordVisible }) {
								Icon(
									painter = painterResource(
										id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
									),
									contentDescription = null
								)
							}
						}
					)

					Spacer(modifier = Modifier.height(8.dp))

					OutlinedTextField(
						value = confirmPassword,
						onValueChange = {
							confirmPassword = it
							errorMessage = null
						},
						label = { Text("Confirmar contraseña") },
						leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
						singleLine = true,
						modifier = Modifier.fillMaxWidth(),
						visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
					)

					Spacer(modifier = Modifier.height(8.dp))

					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Start,
						modifier = Modifier.fillMaxWidth()
					) {
						Checkbox(
							checked = acceptTerms,
							onCheckedChange = {
								acceptTerms = it
								errorMessage = null
							}
						)
						Text("Acepto los términos y condiciones")
					}

					errorMessage?.let {
						Text(it, color = MaterialTheme.colorScheme.error)
						Spacer(modifier = Modifier.height(8.dp))
					}

					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(48.dp)
							.background(
								brush = Brush.horizontalGradient(listOf(PrimaryLight, PrimaryLight2)),
								shape = RoundedCornerShape(12.dp)
							)
							.clickable(enabled = !isLoading) {
								when {
									fullName.isBlank() -> errorMessage = "Ingresa tu nombre"
									!email.contains("@") -> errorMessage = "Correo inválido"
									password.length < 6 -> errorMessage = "La contraseña debe tener al menos 6 caracteres"
									password != confirmPassword -> errorMessage = "Las contraseñas no coinciden"
									!acceptTerms -> errorMessage = "Debes aceptar los términos"
									else -> {
										isLoading = true
										errorMessage = null
										val saved = authRepository.register(
											fullName = fullName,
											email = email.trim(),
											password = password
										)

										if (!saved) {
											errorMessage = "No se pudo guardar el registro"
											isLoading = false
										} else {
											// intentar auto-login inmediatamente
											val loggedIn = authRepository.login(
												LoginCredentials(email = email.trim(), password = password, rememberMe = true)
											)
											isLoading = false
											if (loggedIn) {
												onRegisterSuccess(email.trim())
											} else {
												errorMessage = "Registro guardado pero no se pudo iniciar sesión automáticamente"
											}
										}
									}
								}
							},
						contentAlignment = Alignment.Center
					) {
						if (isLoading) {
							CircularProgressIndicator(
								modifier = Modifier.size(18.dp),
								strokeWidth = 2.dp,
								color = Color.White
							)
						} else {
							Text("Crear cuenta", color = Color.White)
						}
					}

					Spacer(modifier = Modifier.height(8.dp))

					TextButton(
						onClick = onBackToLogin,
						modifier = Modifier.align(Alignment.CenterHorizontally)
					) {
						Text("Ya tengo una cuenta")
					}
				}
			}
		}
	}
}