package lol.gito.embedex.web.dto

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

data class TagDto(
    val id: String,
    val name: String? = null,
    val items: List<IdentifierDto>
)

data class IdentifierDto (
    val id: String,
    val name: String? = null,
)
