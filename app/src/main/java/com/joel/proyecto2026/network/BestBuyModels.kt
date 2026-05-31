package com.joel.proyecto2026.network

data class ProductDto(
    val title: String? = null,
    val price: String? = null,
    val extractedPrice: Double? = null,
    val thumbnail: String? = null,
    val rating: Double? = null,
    val reviews: Int? = null,
    val source: String? = null
)

data class CategoryDto(
    val query: String? = null,
    val name: String? = null,
    val link: String? = null
)
