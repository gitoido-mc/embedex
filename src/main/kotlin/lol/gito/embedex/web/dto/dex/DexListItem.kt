package lol.gito.embedex.web.dto.dex

import com.cobblemon.mod.common.pokemon.Species
import lol.gito.embedex.EmbeDEX

data class DexListItem(
    val dex: Int,
    val name: String,
    val displayName: String,
    val primaryType: String,
    val secondaryType: String?,
    val forms: Int,
    val labels: List<String>
) {
    companion object {
        fun fromSpecies(species: Species): DexListItem = DexListItem(
            species.nationalPokedexNumber,
            species.resourceIdentifier.path,
            species.translatedName.string,
            species.primaryType.name,
            species.secondaryType?.name,
            species.forms.count(),
            EmbeDEX.speciesLabelsHolder.getOrElse(species.resourceIdentifier.path) { emptyList() }
        )
    }
}