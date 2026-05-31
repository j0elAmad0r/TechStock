package com.joel.proyecto2026.repository

import com.joel.proyecto2026.network.CategoryDto
import com.joel.proyecto2026.network.ProductDto

interface BestBuyRepository {
    suspend fun getCategories(query: String = "Procesadores"): List<CategoryDto>
    suspend fun getFeaturedProducts(query: String = "Procesadores"): List<ProductDto>
}
