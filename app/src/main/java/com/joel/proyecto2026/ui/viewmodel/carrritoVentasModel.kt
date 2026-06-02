package com.joel.proyecto2026.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.joel.proyecto2026.network.ProductDto

data class ItemCarrito(
    val producto: ProductDto,
    var cantidad: Int = 1
)

class CarritoViewModel : ViewModel() {
    val articulos = mutableStateListOf<ItemCarrito>()

    fun agregarAlCarrito(producto: ProductDto, cantidad: Int = 1) {
        val idx = articulos.indexOfFirst { it.producto.title == producto.title && it.producto.source == producto.source }
        if (idx >= 0) {
            val existente = articulos[idx]
            existente.cantidad += cantidad
            articulos[idx] = existente
        } else {
            articulos.add(ItemCarrito(producto = producto, cantidad = cantidad))
        }
    }

    fun eliminarItem(producto: ProductDto) {
        val idx = articulos.indexOfFirst { it.producto.title == producto.title && it.producto.source == producto.source }
        if (idx >= 0) articulos.removeAt(idx)
    }

    fun vaciar() {
        articulos.clear()
    }

    fun totalArticulos(): Int = articulos.sumOf { it.cantidad }
}
