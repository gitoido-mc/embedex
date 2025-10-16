package lol.gito.embedex.web.dto.dex

// Package that holds DTOs for various simple representations

data class DexAbility(val id: String, val name: String, val description: String? = null)
data class DexMove(
    val id: String,
    val name: String,
    val description: String? = null,
    val power: Double,
    val accuracy: Double,
    val critRatio: Double,
    val maxPp: Int
)
