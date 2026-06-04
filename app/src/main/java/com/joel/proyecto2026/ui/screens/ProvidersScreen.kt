package com.joel.proyecto2026.ui.screens
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.ui.viewmodel.HomeViewModel

private data class ProviderUi(
    val name: String,
    val products: List<ProductDto>,
    val accent: Color,
    val imageUrl: String? = products.firstNotNullOfOrNull {
        it.sourceIcon?.takeIf(String::isNotBlank)
            ?: it.thumbnail?.takeIf(String::isNotBlank)
    }
) {
    val initials: String
        get() = name
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "PV" }

    val productCount: Int
        get() = products.size

    val categoriesLabel: String
        get() = products
            .mapNotNull { it.title }
            .map { detectCategory(it) }
            .distinct()
            .take(3)
            .joinToString(" • ")
            .ifBlank { "Componentes" }

    val lowStockCount: Int
        get() = products.count { getFakeStock(it) <= 10 }

    val lowStockProducts: List<ProductDto>
        get() = products.filter { getFakeStock(it) <= 10 }

    val stockStatus: String
        get() = when {
            lowStockCount >= 3 -> "Stock crítico"
            lowStockCount >= 1 -> "Revisar stock"
            else -> "Stock estable"
        }

    val reliabilityScore: Double
        get() = (4.1 + productCount.coerceAtMost(8) * 0.08).coerceAtMost(4.9)

    val deliveryLabel: String
        get() = when {
            productCount >= 8 -> "24h"
            productCount >= 4 -> "48h"
            else -> "72h"
        }

    val isPreferred: Boolean
        get() = productCount >= 5 && reliabilityScore >= 4.5
}

private fun buildProvidersFromApi(products: List<ProductDto>): List<ProviderUi> {
    val cleanProducts = products.filter {
        !it.source.isNullOrBlank() && !it.title.isNullOrBlank()
    }

    val grouped = cleanProducts.groupBy { it.source!!.trim() }

    val accents = listOf(
        Color(0xFF4DA3FF),
        Color(0xFF38D996),
        Color(0xFF7C4DFF),
        Color(0xFFFFA64D),
        Color(0xFFFF4D6D)
    )

    return grouped.entries
        .mapIndexed { index, entry ->
            ProviderUi(
                name = entry.key,
                products = entry.value,
                accent = accents[index % accents.size]
            )
        }
        .sortedWith(
            compareByDescending<ProviderUi> { it.isPreferred }
                .thenByDescending { it.productCount }
                .thenBy { it.name }
        )
}

private fun detectCategory(title: String): String {
    val t = title.lowercase()

    return when {
        "rtx" in t || "geforce" in t || "radeon" in t || "gpu" in t -> "Tarjetas gráficas"
        "ryzen" in t || "intel" in t || "core i" in t || "cpu" in t -> "Procesadores"
        "ram" in t || "ddr" in t || "memory" in t -> "Memorias"
        "ssd" in t || "nvme" in t || "hdd" in t -> "Almacenamiento"
        "motherboard" in t || "b550" in t || "b650" in t || "z790" in t -> "Placas base"
        "power" in t || "psu" in t || "fuente" in t -> "Fuentes"
        "sensor" in t || "arduino" in t -> "Sensores"
        else -> "Componentes"
    }
}

private fun getFakeStock(product: ProductDto): Int {
    return ((product.reviews ?: 0) / 10).coerceAtLeast(1)
}

@Composable
fun PantallaProveedores(
    homeViewModel: HomeViewModel? = null,
    onBack: (() -> Unit)? = null
) {
    val defaultQuery = "pc components"
    var search by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<ProviderUi?>(null) }
    var requestedProviderName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(homeViewModel) {
        if (
            homeViewModel != null &&
            homeViewModel.featured.isEmpty() &&
            !homeViewModel.isLoading.value
        ) {
            homeViewModel.search(defaultQuery)
        }
    }

    val apiProducts = homeViewModel?.featured?.toList().orEmpty()

    val providers by remember(apiProducts) {
        derivedStateOf {
            buildProvidersFromApi(apiProducts)
        }
    }

    val filteredProviders by remember(search, providers) {
        derivedStateOf {
            if (search.isBlank()) {
                providers
            } else {
                providers.filter { provider ->
                    provider.name.contains(search, ignoreCase = true) ||
                            provider.categoriesLabel.contains(search, ignoreCase = true)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF030A1D),
                        Color(0xFF0A1A43),
                        Color(0xFF040C23)
                    )
                )
            )
    ) {
        BackgroundGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ProvidersHeader(onBack = onBack)

            Spacer(modifier = Modifier.height(12.dp))

            SearchProviderBar(
                value = search,
                onValueChange = { search = it },
                onSearchClick = {
                    val query = search.ifBlank { defaultQuery }
                    homeViewModel?.search(query)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProviderSummaryCards(
                providers = providers,
                products = apiProducts
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Proveedores encontrados",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${filteredProviders.size}",
                    color = Color(0xFF4DA3FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                homeViewModel?.isLoading?.value == true -> {
                    LoadingProviders()
                }

                providers.isEmpty() -> {
                    EmptyProviders(
                        onSearchClick = {
                            homeViewModel?.search(defaultQuery)
                        }
                    )
                }

                filteredProviders.isEmpty() -> {
                    EmptySearchResult()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = filteredProviders,
                            key = { it.name }
                        ) { provider ->
                            ProviderCompactCard(
                                provider = provider,
                                onClick = { selectedProvider = provider },
                                onRequestOrder = {
                                    requestedProviderName = provider.name
                                    selectedProvider = provider
                                }
                            )
                        }
                    }
                }
            }
        }

        selectedProvider?.let { provider ->
            ProviderOrderDialog(
                provider = provider,
                orderRequested = requestedProviderName == provider.name,
                onDismiss = { selectedProvider = null },
                onRequestOrder = { requestedProviderName = provider.name }
            )
        }
    }
}

@Composable
private fun BackgroundGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(210.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF236BFF).copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(180.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF38D996).copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun ProvidersHeader(
    onBack: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBack?.invoke() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Text(
            text = "Proveedores",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun SearchProviderBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = {
            Text(
                text = "Buscar proveedores de la API",
                color = Color.White.copy(alpha = 0.45f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.72f)
            )
        },
        trailingIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar",
                    tint = Color(0xFF4DA3FF)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF07111D),
            unfocusedContainerColor = Color(0xFF07111D),
            focusedBorderColor = Color.White.copy(alpha = 0.18f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF4DA3FF)
        )
    )
}

@Composable
private fun ProviderSummaryCards(
    providers: List<ProviderUi>,
    products: List<ProductDto>
) {
    val totalLowStock = products.count { getFakeStock(it) <= 10 }
    val preferredCount = providers.count { it.isPreferred }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniSummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Store,
            title = "Proveedores",
            value = providers.size.toString(),
            accent = Color(0xFF4DA3FF)
        )

        MiniSummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Warning,
            title = "Bajo stock",
            value = totalLowStock.toString(),
            accent = Color(0xFFFFA64D)
        )

        MiniSummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Star,
            title = "Preferidos",
            value = preferredCount.toString(),
            accent = Color(0xFF38D996)
        )
    }
}

@Composable
private fun MiniSummaryCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier.height(92.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProviderCompactCard(
    provider: ProviderUi,
    onClick: () -> Unit = {},
    onRequestOrder: () -> Unit = {}
) {
    val statusColor = when (provider.stockStatus) {
        "Stock crítico" -> Color(0xFFFF4D4D)
        "Revisar stock" -> Color(0xFFFFA64D)
        else -> Color(0xFF38D996)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        border = BorderStroke(
            1.dp,
            if (provider.isPreferred) provider.accent.copy(alpha = 0.6f)
            else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(provider.accent.copy(alpha = 0.13f))
                    .border(
                        width = 1.dp,
                        color = provider.accent.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!provider.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = provider.imageUrl,
                        contentDescription = provider.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = provider.initials,
                        color = provider.accent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = provider.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (provider.isPreferred) {
                        SmallPill(
                            text = "Preferido",
                            color = Color(0xFF4DA3FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = provider.categoriesLabel,
                    color = Color.White.copy(alpha = 0.63f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProviderMiniInfo(
                        icon = Icons.Filled.Inventory2,
                        value = "${provider.productCount + 3 * 2} productos"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    ProviderMiniInfo(
                        icon = Icons.Filled.Shield,
                        value = String.format("%.1f / 5", provider.reliabilityScore)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    ProviderMiniInfo(
                        icon = Icons.Filled.Store,
                        value = provider.deliveryLabel
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                SmallPill(
                    text = provider.stockStatus,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )

                if (provider.lowStockCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ReorderButton(onClick = onRequestOrder)
                }
            }
        }
    }
}

@Composable
private fun ReorderButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF4DA3FF).copy(alpha = 0.16f))
            .border(
                width = 1.dp,
                color = Color(0xFF4DA3FF).copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocalShipping,
            contentDescription = null,
            tint = Color(0xFF4DA3FF),
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun ProviderOrderDialog(
    provider: ProviderUi,
    orderRequested: Boolean,
    onDismiss: () -> Unit,
    onRequestOrder: () -> Unit
) {
    val productsToOrder = provider.lowStockProducts.ifEmpty { provider.products.take(3) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111E)),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, provider.accent.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProviderAvatar(provider = provider, size = 54)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = provider.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Reposicion de inventario",
                            color = Color.White.copy(alpha = 0.62f),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (orderRequested) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF38D996).copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF38D996).copy(alpha = 0.34f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF38D996),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pedido solicitado para rellenar stock.",
                            color = Color(0xFF38D996),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Productos sugeridos",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    productsToOrder.take(4).forEach { product ->
                        OrderProductRow(product = product, accent = provider.accent)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onRequestOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !orderRequested,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4DA3FF),
                        disabledContainerColor = Color(0xFF38D996).copy(alpha = 0.22f),
                        disabledContentColor = Color(0xFF38D996)
                    )
                ) {
                    Icon(
                        imageVector = if (orderRequested) Icons.Filled.CheckCircle else Icons.Filled.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (orderRequested) "Solicitud enviada" else "Realizar pedido",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderAvatar(provider: ProviderUi, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(provider.accent.copy(alpha = 0.13f))
            .border(
                width = 1.dp,
                color = provider.accent.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!provider.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = provider.imageUrl,
                contentDescription = provider.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = provider.initials,
                color = provider.accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OrderProductRow(product: ProductDto, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121824))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            if (!product.thumbnail.isNullOrBlank()) {
                AsyncImage(
                    model = product.thumbnail,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = accent)
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title ?: "Producto",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Stock actual: ${getFakeStock(product)}",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ProviderMiniInfo(
    icon: ImageVector,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.48f),
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = value,
            color = Color.White.copy(alpha = 0.66f),
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun SmallPill(
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .widthIn(min = 74.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.13f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.38f),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun LoadingProviders() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cargando proveedores desde la API...",
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun EmptyProviders(
    onSearchClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable { onSearchClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Store,
                contentDescription = null,
                tint = Color(0xFF4DA3FF),
                modifier = Modifier.size(38.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No hay proveedores cargados",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Toca aquí para consultar proveedores desde la API.",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptySearchResult() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se encontraron proveedores con esa búsqueda.",
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaPantallaProveedores() {
    val sample = listOf(
        ProviderUi("Distribuidor A", emptyList(), Color(0xFF4DA3FF)),
        ProviderUi("Distribuidor B", emptyList(), Color(0xFF38D996)),
        ProviderUi("Distribuidor C", emptyList(), Color(0xFF7C4DFF)),
        ProviderUi("Distribuidor D", emptyList(), Color(0xFFFFA64D))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF030A1D), Color(0xFF0A1A43))
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProviderSummaryCards(providers = sample, products = emptyList())

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sample) { p ->
                    ProviderCompactCard(provider = p)
                }
            }
        }
    }
}
