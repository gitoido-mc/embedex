package lol.gito.embedex.web.dto.dex

import com.cobblemon.mod.common.pokemon.Species

data class DexList(
    val species: List<DexListItem>,
    val labels: List<String>,
    val types: List<String>,
    val search: String?,
    val perPage: Int,
    val page: Int,
    val total: Int,
    val lastPage: Int
) {
    companion object {
        fun fromSpeciesList(
            list: List<Species>,
            labels: List<String>,
            types: List<String>,
            search: String?,
            perPage: Int,
            page: Int,
            total: Int,
            lastPage: Int
        ): DexList = DexList(
            list.map { DexListItem.fromSpecies(it) },
            labels,
            types,
            search,
            perPage,
            page,
            total,
            lastPage
        )
    }
}
