package lol.gito.embedex.web.dto.dex

import com.cobblemon.mod.common.pokemon.Species

data class DexListItem(
    val dex: Int,
    val name: String,
    val displayName: String,
    val primaryType: String,
    val secondaryType: String?,
    val forms: Int,
    val labels: HashSet<String>
) {
    companion object {
        fun fromSpecies(species: Species): DexListItem = DexListItem(
            species.nationalPokedexNumber,
            species.resourceIdentifier.path,
            species.translatedName.string,
            species.primaryType.name,
            species.secondaryType?.name,
            species.forms.count(),
            species.labels
        )
    }
}