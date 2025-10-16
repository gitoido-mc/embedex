package lol.gito.embedex.web.dto.dex.evolution

import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.PassiveEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.BlockClickEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution
import com.cobblemon.mod.common.registry.BlockIdentifierCondition
import com.cobblemon.mod.common.registry.BlockTagCondition
import com.cobblemon.mod.common.registry.ItemIdentifierCondition
import com.cobblemon.mod.common.registry.ItemTagCondition
import lol.gito.embedex.EmbeDEX
import lol.gito.embedex.web.dto.dex.evolution.requirement.RequirementHolder

data class EvolutionInfo(
    val id: String,
    val variant: String,
    val result: EvolutionResult,
    val consumeHeldItem: Boolean = false,
    val learnableMoves: List<String>? = null,
    val requirements: List<RequirementHolder>? = null,
    val requiredContext: String? = null
) {
    companion object {
        fun fromEvolution(evolution: Evolution): EvolutionInfo? {
            val result = EmbeDEX.speciesHolder.find { it ->
                it.resourceIdentifier.path == evolution.result.species
            }

            return when (result) {
                null -> null
                else -> {
                    val context = when (evolution) {
                        is BlockClickEvolution -> when (evolution.requiredContext) {
                            is BlockIdentifierCondition -> (evolution.requiredContext as BlockIdentifierCondition).identifier.toString()
                            is BlockTagCondition -> (evolution.requiredContext as BlockTagCondition).tag.id.toString()
                            else -> null
                        }

                        is ItemInteractionEvolution -> when (evolution.requiredContext.item) {
                            is ItemIdentifierCondition -> (evolution.requiredContext.item as ItemIdentifierCondition).identifier.toString()
                            is ItemTagCondition -> (evolution.requiredContext.item as ItemTagCondition).tag.id.toString()
                            else -> null
                        }

                        else -> null
                    }


                    EvolutionInfo(
                        id = evolution.id,
                        variant = when (evolution) {
                            is BlockClickEvolution -> "block_click"
                            is ItemInteractionEvolution -> "item_interact"
                            is TradeEvolution -> "trade"
                            is LevelUpEvolution -> "level_up"
                            // `when` bails immediately on match, so we put LevelUpEvo earlier
                            is PassiveEvolution -> "passive"
                            else -> "dummy"
                        },
                        result = EvolutionResult(
                            dex = result.nationalPokedexNumber,
                            name = result.resourceIdentifier.path,
                            displayName = result.translatedName.string,
                        ),
                        consumeHeldItem = evolution.consumeHeldItem,
                        learnableMoves = evolution.learnableMoves.map { it.name }.toList(),
                        requirements = evolution.requirements.mapNotNull { RequirementHolder.fromRequirement(it) }.let {
                            when (it.isEmpty()) {
                                true -> null
                                false -> it
                            }
                        },
                        requiredContext = context
                    )
                }
            }
        }
    }
}
