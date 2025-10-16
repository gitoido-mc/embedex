package lol.gito.embedex.web.dto.dex

import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.util.asTranslated
import lol.gito.embedex.web.dto.dex.DexDetail.Drops
import lol.gito.embedex.web.dto.dex.DexDetail.ItemDrops
import lol.gito.embedex.web.dto.dex.DexDetail.StatHolder

data class DexForm(
    val name: String,
    val primaryType: String,
    val secondaryType: String?,
    val height: Float,
    val weight: Float,
    val baseStats: StatHolder,
    val labels: Set<String>,
    val drops: Drops?,
) {
    companion object {
        fun fromForm(form: FormData): DexForm {
            return DexForm(
                name = form.name,
                primaryType = form.primaryType.name,
                secondaryType = form.secondaryType?.name,
                height = form.height,
                weight = form.weight,
                baseStats = StatHolder.fromSpeciesStats(form.baseStats),
                labels = form.labels,
                drops = Drops(
                    form.drops.amount.last,
                    form.drops.entries.mapNotNull { item ->
                        when (item) {
                            is ItemDropEntry -> ItemDrops(
                                item.item.toString(),
                                "item.${item.item.toTranslationKey()}".asTranslated().string,
                                item.percentage,
                                item.components?.let {
                                    val res = HashMap<String, Any?>()

                                    it.forEach { component ->
                                        res.put(component.type.toString(), component.value)
                                    }

                                    return@let res
                                },
                                item.quantityRange,
                                item.dropMethod?.name
                            )
                            else -> null
                        }
                    }
                )
            )
        }
    }
}
