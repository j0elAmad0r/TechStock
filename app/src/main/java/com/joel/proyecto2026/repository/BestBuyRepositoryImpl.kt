package com.joel.proyecto2026.repository

import com.joel.proyecto2026.network.CategoryDto
import com.joel.proyecto2026.network.ProductDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BestBuyRepositoryImpl(private val apiKey: String) : BestBuyRepository {
    private val client = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build()

    // TODO: ¡PEGA AQUÍ TU ENLACE DE NGROK! (Conserva el /techstock al final)
    private val BASE_URL_NGROK = "https://horologic-subreniform-angelika.ngrok-free.dev/techstock"

    private fun buildSerpUrl(query: String): String {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        return "https://serpapi.com/search?engine=google_shopping&q=$encodedQuery&location=Mexico&api_key=$apiKey"
    }

    override suspend fun getCategories(query: String): List<CategoryDto> {
        return listOf(
            CategoryDto("Procesadores", "Procesadores", ""),
            CategoryDto("Tarjetas Gráficas", "Tarjetas Gráficas", ""),
            CategoryDto("Almacenamiento", "Almacenamiento", "")
        )
    }

    // ==========================================
    // Inventario que el cliente ve
    // ==========================================
    override suspend fun getFeaturedProducts(query: String): List<ProductDto> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$BASE_URL_NGROK/productos.php").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList<ProductDto>()

                val json = JSONObject(response.body?.string().orEmpty())
                if (!json.optBoolean("success", false)) return@use emptyList<ProductDto>()

                val results = json.optJSONArray("data") ?: return@withContext emptyList()

                buildList {
                    for (index in 0 until results.length()) {
                        val item = results.optJSONObject(index) ?: continue

                        val precioReal = item.optDouble("precio", 0.0)

                        add(ProductDto(
                            title = item.optString("nombre_producto", "Producto sin nombre"),
                            price = "$" + String.format("%.2f", precioReal),
                            extractedPrice = precioReal,
                            thumbnail = item.optString("imagen_url", "cpu"), // Aquí jala tus imágenes locales (cpu, gpu, ssd)
                            rating = 5.0,
                            reviews = (50..200).random(),
                            source = item.optString("marca", "TechStock"),
                            sourceIcon = ""
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ==========================================
    // Bucador de provedores para el admin
    // ==========================================
    override suspend fun searchProviders(query: String): List<ProductDto> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext emptyList()
        try {
            val request = Request.Builder().url(buildSerpUrl(query)).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList<ProductDto>()
                val json = JSONObject(response.body?.string().orEmpty())
                val results = json.optJSONArray("shopping_results") ?: return@withContext emptyList()

                buildList {
                    for (index in 0 until minOf(results.length(), 10)) {
                        val item = results.optJSONObject(index) ?: continue
                        add(ProductDto(
                            title = item.optString("title").ifBlank { item.optString("name") },
                            price = item.optString("price").ifBlank {
                                item.optDouble("extracted_price", Double.NaN).takeIf { !it.isNaN() }?.let { "$" + String.format("%.2f", it) }.orEmpty()
                            },
                            extractedPrice = item.optDouble("extracted_price", Double.NaN).takeIf { !it.isNaN() },
                            thumbnail = item.optString("thumbnail").ifBlank { item.optString("image") },
                            rating = item.optDouble("rating", Double.NaN).takeIf { !it.isNaN() },
                            reviews = item.optInt("reviews", -1).takeIf { it >= 0 },
                            source = item.optString("source"),
                            sourceIcon = item.optString("source_icon").ifBlank { item.optString("source_logo") }
                        ))
                    }
                }
            }
        } catch (e: Exception) { emptyList() }
    }

    // ==========================================
    // Procesar las compra por el admin
    // ==========================================
    override suspend fun restockProduct(product: ProductDto, nombreFinal: String, precioVenta: Double, idCategoria: Int, cantidad: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonParam = JSONObject().apply {
                put("nombre_final", nombreFinal)
                put("marca", product.source ?: "Proveedor")
                put("precio_compra", product.extractedPrice ?: 0.0)
                put("precio_venta", precioVenta)
                put("id_categoria", idCategoria)
                put("cantidad", cantidad)
                put("imagen_url", product.thumbnail)
            }
            val requestBody = jsonParam.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url("$BASE_URL_NGROK/restock.php").post(requestBody).build()

            // Guardamos el resultado de la llamada en una variable con su if / else completo
            val exito = client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respJson = JSONObject(response.body?.string().orEmpty())
                    respJson.optBoolean("success", false)
                } else {
                    false
                }
            }
            return@withContext exito
        } catch (e: Exception) {
            return@withContext false
        }
    }
}