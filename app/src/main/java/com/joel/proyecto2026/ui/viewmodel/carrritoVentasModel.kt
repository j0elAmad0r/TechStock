package com.joel.proyecto2026.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.joel.proyecto2026.network.ProductDto

data class ItemCarrito(
    val producto: ProductDto,
    val cantidad: Int = 1
)

class CarritoViewModel : ViewModel() {

    val articulos = mutableStateListOf<ItemCarrito>()

    fun agregarAlCarrito(producto: ProductDto, cantidad: Int = 1) {
        val index = articulos.indexOfFirst {
            it.producto.title == producto.title &&
                    it.producto.source == producto.source
        }

        if (index >= 0) {
            val itemActual = articulos[index]
            articulos[index] = itemActual.copy(
                cantidad = itemActual.cantidad + cantidad
            )
        } else {
            articulos.add(
                ItemCarrito(
                    producto = producto,
                    cantidad = cantidad
                )
            )
        }
    }

    fun aumentarCantidad(producto: ProductDto) {
        val index = articulos.indexOfFirst {
            it.producto.title == producto.title &&
                    it.producto.source == producto.source
        }

        if (index >= 0) {
            val itemActual = articulos[index]
            articulos[index] = itemActual.copy(
                cantidad = itemActual.cantidad + 1
            )
        }
    }

    fun disminuirCantidad(producto: ProductDto) {
        val index = articulos.indexOfFirst {
            it.producto.title == producto.title &&
                    it.producto.source == producto.source
        }

        if (index >= 0) {
            val itemActual = articulos[index]

            if (itemActual.cantidad <= 1) {
                articulos.removeAt(index)
            } else {
                articulos[index] = itemActual.copy(
                    cantidad = itemActual.cantidad - 1
                )
            }
        }
    }

    fun eliminarItem(producto: ProductDto) {
        val index = articulos.indexOfFirst {
            it.producto.title == producto.title &&
                    it.producto.source == producto.source
        }

        if (index >= 0) {
            articulos.removeAt(index)
        }
    }

    fun vaciar() {
        articulos.clear()
    }

    fun totalArticulos(): Int {
        return articulos.sumOf { it.cantidad }
    }

    fun subtotal(): Double {
        return articulos.sumOf { item ->
            obtenerPrecio(item.producto) * item.cantidad
        }
    }

    fun iva(): Double {
        return subtotal() * 0.16
    }

    fun total(): Double {
        return subtotal() + iva()
    }

    private fun obtenerPrecio(producto: ProductDto): Double {
        return producto.extractedPrice
            ?: producto.price
                ?.replace("$", "")
                ?.replace(",", "")
                ?.trim()
                ?.toDoubleOrNull()
            ?: 0.0
    }
}