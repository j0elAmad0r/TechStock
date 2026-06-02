package com.joel.proyecto2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings

@Composable
fun PantallaConfiguracion(
    onBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = Color(0xFF030A1D)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Configuración del Sistema", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Administra los ajustes globales de la aplicación.", color = Color.White.copy(alpha = 0.66f), fontSize = 13.sp)
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF07111D))
                        .clickable { /* quick action */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes", tint = Color(0xFF4DA3FF))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Theme card
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF352B6B)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFFBFA8FF))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Cambiar tema", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(text = "Personaliza la apariencia de la aplicación.", color = Color.White.copy(alpha = 0.66f), fontSize = 13.sp)
                    }

                    // segmented mock
                    Row(modifier = Modifier.widthIn(min = 160.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val selected = remember { mutableStateOf("Oscuro") }
                        ThemeToggleButton(text = "Oscuro", selected = selected.value == "Oscuro") { selected.value = "Oscuro" }
                        ThemeToggleButton(text = "Claro", selected = selected.value == "Claro") { selected.value = "Claro" }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Company card
            SectionCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF163A8A)), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = Color(0xFF7FB2FF))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Configuración de empresa", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(text = "Gestiona la información y preferencias de tu empresa.", color = Color.White.copy(alpha = 0.66f), fontSize = 13.sp)
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = Color.White.copy(alpha = 0.04f))

                    RowItem(icon = Icons.Default.Store, label = "Empresa", value = "TechStock Solutions")
                    RowItem(icon = Icons.Default.Person, label = "RFC", value = "TEC230101AB4")
                    RowItem(icon = Icons.Default.Place, label = "Ubicación", value = "Ciudad de México, México")
                    RowItem(icon = Icons.Default.Email, label = "Correo de contacto", value = "contacto@techstock.com")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Backup card
            SectionCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0A5E3A)), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF7BE7B7))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Respaldos", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(text = "Administra los respaldos de la información.", color = Color.White.copy(alpha = 0.66f), fontSize = 13.sp)
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha = 0.04f))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Último respaldo", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(text = "11 de mayo de 2026 • 02:30 AM", color = Color.White.copy(alpha = 0.66f), fontSize = 13.sp)
                        }

                        AccentOutlinedButton(text = "Realizar respaldo ahora", onClick = {})
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // API card
            SectionCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF4A2B65)), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xFFCFB3FF))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Configuración de API", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(text = "Administra las claves y conexiones de las APIs externas.", color = Color.White.copy(alpha = 0.66f), fontSize = 13.sp)
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha = 0.04f))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "SerpAPI (Productos)", color = Color.White)
                            Text(text = "••••••••••••••••3456", color = Color(0xFFCFB3FF), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(onClick = { /* manage key */ }, shape = RoundedCornerShape(12.dp)) {
                            Text(text = "Gestionar clave", color = Color(0xFF9B5CFF))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Estado: ", color = Color.White.copy(alpha = 0.66f))
                        Text(text = "Conectado", color = Color(0xFF4DA3FF), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Última verificación: Hoy, 09:42 AM", color = Color.White.copy(alpha = 0.66f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout button
            OutlinedButton(
                onClick = { onLogout?.invoke() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar sesión", color = Color(0xFFFF6B6B))
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, defaultOn: Boolean) {
    val state = remember { mutableStateOf(defaultOn) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF4DA3FF), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(10.dp))
            Text(text = label, color = Color.White, fontSize = 15.sp)
        }

        Switch(checked = state.value, onCheckedChange = { state.value = it })
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaPantallaConfiguracion() {
    PantallaConfiguracion(onBack = {})
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07111D)),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
private fun ThemeToggleButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFF0C2B4D) else Color(0xFF07111D)
    val border = if (selected) Color(0xFF4DA3FF) else Color.White.copy(alpha = 0.06f)
    Box(modifier = Modifier
        .width(72.dp)
        .height(36.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(bg)
        .clickable { onClick() }
        .border(width = 1.dp, color = border, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Text(text = text, color = if (selected) Color(0xFF4DA3FF) else Color.White.copy(alpha = 0.72f))
    }
}

@Composable
private fun RowItem(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF4DA3FF), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.78f), modifier = Modifier.weight(1f))
        Text(text = value, color = Color.White.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AccentOutlinedButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(12.dp)) {
        Text(text = text, color = Color(0xFF15C27A))
    }
}
