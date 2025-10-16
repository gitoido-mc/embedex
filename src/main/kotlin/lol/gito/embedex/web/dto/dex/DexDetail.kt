package lol.gito.embedex.web.dto.dex

import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import lol.gito.embedex.web.dto.dex.evolution.EvolutionInfo
import lol.gito.embedex.web.dto.dex.evolution.EvolutionResult
import lol.gito.embedex.web.dto.dex.spawn.SpawnHolder

data class DexDetail(
    val dex: Int,
    val name: String,
    val displayName: String,
    val primaryType: String,
    val secondaryType: String?,
    val maleRatio: Float,
    val height: Float,
    val weight: Float,
    val expGroup: String,
    val catchRate: Int,
    val eggGroups: List<String>,
    val eggCycles: Int,
    val baseStats: StatHolder,
    val evYield: StatHolder,
    val forms: List<DexForm>?,
    val labels: HashSet<String>,
    val drops: Drops?,
    val preEvolution: EvolutionResult?,
    val evolutions: List<EvolutionInfo>,
    val spawn: List<SpawnHolder>
) {
    data class StatHolder(
        val hp: Int,
        val attack: Int,
        val defence: Int,
        val specialAttack: Int,
        val specialDefence: Int,
        val speed: Int,
        val evasion: Int,
        val accuracy: Int
    ) {
        companion object {
            fun fromSpeciesStats(baseStats: Map<Stat, Int>): StatHolder = StatHolder(
                hp = baseStats[Stats.getStat("hp")] ?: 0,
                attack = baseStats[Stats.getStat("atk")] ?: 0,
                defence = baseStats[Stats.getStat("def")] ?: 0,
                specialAttack = baseStats[Stats.getStat("spa")] ?: 0,
                specialDefence = baseStats[Stats.getStat("spd")] ?: 0,
                speed = baseStats[Stats.getStat("speed")] ?: 0,
                evasion = baseStats[Stats.getStat("evasion")] ?: 0,
                accuracy = baseStats[Stats.getStat("accuracy")] ?: 0,
            )
        }
    }

    data class Drops(val amount: Int, val entries: List<DropsEntry>)

    @Suppress("unused")
    open class DropsEntry(open val item: String, open val percentage: Float?, val quantity: Int? = null)
    data class ItemDrops(
        @Transient
        override val item: String,
        val name: String,
        @Transient
        override val percentage: Float?,
        val components: Map<String, *>? = null,
        val quantityRange: IntRange? = null,
        val dropMethod: String?
    ) : DropsEntry(item, percentage)

    companion object {
        fun fromSpecies(species: Species): DexDetail {
            val spawns: List<SpawnDetail> = CobblemonSpawnPools.WORLD_SPAWN_POOL.details.filter { spawnDetail ->
                true
            }.filter { spawnDetail ->
                (spawnDetail as PokemonSpawnDetail).pokemon.species == species.resourceIdentifier.path
            }

            return DexDetail(
                dex = species.nationalPokedexNumber,
                name = species.resourceIdentifier.path,
                displayName = species.translatedName.string,
                primaryType = species.primaryType.displayName.string,
                secondaryType = species.secondaryType?.displayName?.string,
                maleRatio = species.maleRatio,
                height = species.height,
                weight = species.weight,
                expGroup = species.experienceGroup.name,
                catchRate = species.catchRate,
                eggGroups = species.eggGroups.map { it.name },
                eggCycles = species.eggCycles,
                baseStats = StatHolder.fromSpeciesStats(species.baseStats),
                evYield = StatHolder.fromSpeciesStats(species.evYield),
                labels = species.labels,
                preEvolution = species.preEvolution?.let {
                    EvolutionResult(
                        dex = it.species.nationalPokedexNumber,
                        name = it.species.resourceIdentifier.path,
                        displayName = it.species.translatedName.string
                    )
                },
                evolutions = species.evolutions
                    .mapNotNull { evolution -> EvolutionInfo.fromEvolution(evolution) }
                    .toList(),
                spawn = spawns.map { SpawnHolder.fromSpawnDetail(it) },
                forms = species.forms.map { DexForm.fromForm(it) },
                drops = Drops(
                    species.drops.amount.last,
                    species.drops.entries.mapNotNull { item ->
                        when (item) {
                            is ItemDropEntry -> ItemDrops(
                                item.item.toString(),
                                item.item.toTranslationKey().asTranslated().string,
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
