package com.joel.proyecto2026.network

import retrofit2.http.GET
import retrofit2.http.Query

data class ProductsResponse(
    val products: List<ProductDto>? = null
)

data class CategoriesResponse(
    val categories: List<CategoryDto>? = null
)

interface BestBuyApi {
    @GET("v1/categories")
    suspend fun getCategories(
        @Query("format") format: String = "json",
        @Query("apiKey") apiKey: String,
        @Query("show") show: String = "id,name,fullName,path"
    ): CategoriesResponse

    @GET("v1/products")
    suspend fun getProducts(
        @Query("format") format: String = "json",
        @Query("apiKey") apiKey: String,
        @Query("show") show: String = "sku,name,salePrice,thumbnailImage,customerReviewAverage,customerReviewCount",
        @Query("pageSize") pageSize: Int = 8
    ): ProductsResponse
}
