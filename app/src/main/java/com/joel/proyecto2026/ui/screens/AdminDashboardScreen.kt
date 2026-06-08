package com.joel.proyecto2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminDashboardScreen(
    userName: String?,
    onOpenInventory: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenStatistics: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF030A1D), Color(0xFF0A1A43))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header del Admin
            Text(
                text = "Panel de Control",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modo Administrador | $userName",
                color = Color(0xFF38D996),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(42.dp))

            // Cuadrícula estilo Streamdeck
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StreamDeckButton(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    title = "Inventario",
                    icon = Icons.Filled.Inventory,
                    color1 = Color(0xFF4DA3FF),
                    color2 = Color(0xFF2B77FF),
                    onClick = onOpenInventory
                )
                StreamDeckButton(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    title = "Proveedores",
                    icon = Icons.Filled.LocalShipping,
                    color1 = Color(0xFF38D996),
                    color2 = Color(0xFF1EA871),
                    onClick = onOpenProviders
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StreamDeckButton(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    title = "Estadísticas",
                    icon = Icons.Filled.Analytics, // Si marca error, usa Icons.Filled.TrendingUp
                    color1 = Color(0xFFFFA64D),
                    color2 = Color(0xFFFF7A00),
                    onClick = onOpenStatistics
                )
                // Espacio vacío para mantener el diseño simétrico
                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Barra inferior limpia (Solo Inicio y Perfil)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1420)),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarItem("Inicio", Icons.Filled.Home, true) {}
                    BottomBarItem("Perfil", Icons.Filled.Person, false, onProfileClick)
                }
            }
        }
    }
}

@Composable
private fun StreamDeckButton(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    color1: Color,
    color2: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(color1, color2))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) Color(0xFF4DA3FF).copy(alpha = 0.22f) else Color.Transparent)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color(0xFF4DA3FF) else Color.White.copy(alpha = 0.56f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.58f),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}