package com.joel.proyecto2026.repository

import com.joel.proyecto2026.network.CategoryDto
import com.joel.proyecto2026.network.ProductDto

interface BestBuyRepository {
    suspend fun getCategories(query: String): List<CategoryDto>
    suspend fun getFeaturedProducts(query: String): List<ProductDto> // Esta leerá tu MySQL
    suspend fun searchProviders(query: String): List<ProductDto> // Esta la usaremos después para el Admin
    suspend fun restockProduct(product: ProductDto, nombreFinal: String, precioVenta: Double, idCategoria: Int, cantidad: Int): Boolean
}