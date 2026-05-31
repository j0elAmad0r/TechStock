package com.joel.proyecto2026.ui.viewmodel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joel.proyecto2026.network.CategoryDto
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.repository.BestBuyRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: BestBuyRepository) : ViewModel() {
    val categories = mutableStateListOf<CategoryDto>()
    val featured = mutableStateListOf<ProductDto>()

    
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init {
        search("Procesador")
    }

    fun search(query: String) {
        // mejorar el query para resultados precisos
        fun enhance(q: String): String {
            val base = q.trim()
            if (base.isEmpty()) return base
            val qualifiers = listOf("componentes", "precio", "comprar", "Mexico")
            val toAdd = qualifiers.filter { !base.contains(it, ignoreCase = true) }
            return (listOf(base) + toAdd).joinToString(" ")
        }

        val enhancedQuery = enhance(query)

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val cats = repository.getCategories(enhancedQuery)
                val prods = repository.getFeaturedProducts(enhancedQuery)
                categories.clear()
                categories.addAll(cats)
                featured.clear()
                featured.addAll(prods)
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Error"
            } finally {
                isLoading.value = false
            }
        }
    }
}