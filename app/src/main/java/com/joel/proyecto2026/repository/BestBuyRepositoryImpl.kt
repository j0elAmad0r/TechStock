package com.joel.proyecto2026.repository
import com.joel.proyecto2026.network.CategoryDto
import com.joel.proyecto2026.network.ProductDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BestBuyRepositoryImpl(private val apiKey: String) : BestBuyRepository {
    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildUrl(query: String): String {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        return "https://serpapi.com/search?engine=google_shopping&q=$encodedQuery&location=Mexico&api_key=$apiKey"
    }

    override suspend fun getCategories(query: String): List<CategoryDto> {
        if (apiKey.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(buildUrl(query)).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use emptyList<CategoryDto>()
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val related = json.optJSONArray("related_searches") ?: return@withContext emptyList()
                    buildList {
                        for (index in 0 until minOf(related.length(), 6)) {
                            val item = related.optJSONObject(index) ?: continue
                            val query = item.optString("query").ifBlank { item.optString("name") }
                            if (query.isNotBlank()) {
                                add(
                                    CategoryDto(
                                        query = query,
                                        name = query,
                                        link = item.optString("serpapi_link").takeIf { it.isNotBlank() }
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getFeaturedProducts(query: String): List<ProductDto> {
        if (apiKey.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(buildUrl(query)).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use emptyList<ProductDto>()
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val results = json.optJSONArray("shopping_results") ?: return@withContext emptyList()
                    buildList {
                        for (index in 0 until minOf(results.length(), 8)) {
                            val item = results.optJSONObject(index) ?: continue
                            add(
                                ProductDto(
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
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
