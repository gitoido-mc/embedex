package lol.gito.embedex.web.dto.dex.evolution.requirement

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.pokemon.evolution.requirements.*
import com.cobblemon.mod.common.registry.*

abstract class RequirementHolder(open val variant: String) {
    class Dummy() : RequirementHolder("dummy")
    data class IntegerHolder(@Transient override val variant: String, val amount: Int) : RequirementHolder(variant)
    data class StringHolder(@Transient override val variant: String, val value: String) : RequirementHolder(variant)
    data class Any(val requirements: List<RequirementHolder>) : RequirementHolder("any")
    data class Area(
        val minX: Double,
        val minY: Double,
        val minZ: Double,
        val maxX: Double,
        val maxY: Double,
        val maxZ: Double
    ) : RequirementHolder("area")

    data class AttackDefenceRatio(
        val ratio: AttackDefenceRatioRequirement.AttackDefenceRatio
    ) : RequirementHolder("attack_defence_ratio")

    data class TargetHolder(
        @Transient override val variant: String,
        val target: String,
        val contains: Boolean? = null,
        val amount: Int? = null
    ) : RequirementHolder(variant)

    open class MinMaxHolder(
        @Transient override val variant: String,
        open val min: Int? = 1,
        open val max: Int? = Int.MAX_VALUE
    ) : RequirementHolder(variant)

    data class RangeHolder(
        @Transient override val variant: String,
        @Transient override val min: Int? = 1,
        @Transient override val max: Int? = Int.MAX_VALUE,
        val feature: String? = null
    ) : MinMaxHolder(variant, min, max)

    data class StatCompareHolder(val highStat: String, val lowStat: String) : RequirementHolder("stat_compare")
    data class StatEqualHolder(val leftStat: String, val rightStat: String) : RequirementHolder("stat_equal")
    data class StructureHolder(val shouldBe: String, val shouldNotBe: String) : RequirementHolder("structure")
    data class WeatherHolder(val isRaining: Boolean? = null, val isThundering: Boolean? = null) :
        RequirementHolder("weather")

    companion object {
        fun fromRequirement(requirement: EvolutionRequirement): RequirementHolder? = when (requirement) {
            is AnyRequirement -> Any(
                requirements = requirement.possibilities.mapNotNull { fromRequirement(it) }
            )

            is AreaRequirement -> Area(
                minX = requirement.box.minX,
                minY = requirement.box.minY,
                minZ = requirement.box.minZ,
                maxX = requirement.box.maxX,
                maxY = requirement.box.maxY,
                maxZ = requirement.box.maxZ,
            )

            is AttackDefenceRatioRequirement -> AttackDefenceRatio(requirement.ratio)
            is BattleCriticalHitsRequirement -> IntegerHolder("battle_critical_hits", requirement.amount)
            is BiomeRequirement -> StringHolder(
                "biome",
                when (requirement.biomeCondition) {
                    is BiomeIdentifierCondition -> (requirement.biomeCondition as BiomeIdentifierCondition).identifier.toString()
                    is BiomeTagCondition -> "#" + (requirement.biomeCondition as BiomeTagCondition).tag.id.toString()
                    else -> "minecraft:air"
                }
            )

            is BlocksTraveledRequirement -> IntegerHolder("blocks_traveled", requirement.amount)
            is DamageTakenRequirement -> IntegerHolder("damage_taken", requirement.amount)
            is DefeatRequirement -> TargetHolder(
                "defeat",
                target = requirement.target.asString(),
                amount = requirement.amount
            )

            is FriendshipRequirement -> IntegerHolder("friendship", requirement.amount)
            is HeldItemRequirement -> StringHolder(
                "held_item",
                when (requirement.itemCondition.item) {
                    is ItemIdentifierCondition -> (requirement.itemCondition.item as ItemIdentifierCondition).identifier.toString()
                    is ItemTagCondition -> "#" + (requirement.itemCondition.item as ItemTagCondition).tag.id.toString()
                    else -> "minecraft:air"
                }
            )

            is LevelRequirement -> MinMaxHolder("level", requirement.minLevel, requirement.maxLevel)
            is MoonPhaseRequirement -> StringHolder("moon_phase", requirement.moonPhase.name)
            is MoveSetRequirement -> StringHolder("has_move", requirement.move.name)
            is MoveTypeRequirement -> StringHolder("has_move_type", requirement.type.name)
            is PartyMemberRequirement -> TargetHolder(
                "party_member",
                target = requirement.target.asString(),
                contains = requirement.contains
            )

            is PlayerHasAdvancementRequirement -> StringHolder(
                "advancement",
                requirement.requiredAdvancement.toString()
            )

            is PokemonPropertiesRequirement -> StringHolder("properties", requirement.target.asString())
            is PropertyRangeRequirement -> RangeHolder(
                "property_range",
                min = requirement.range.min(),
                max = requirement.range.max(),
                feature = requirement.feature,
            )

            is RecoilRequirement -> IntegerHolder("recoil", requirement.amount)
            is StatCompareRequirement -> StatCompareHolder(
                lowStat = requirement.lowStat,
                highStat = requirement.highStat
            )

            is StatEqualRequirement -> StatEqualHolder(
                leftStat = requirement.statOne,
                rightStat = requirement.statTwo
            )

            is StructureRequirement -> StructureHolder(
                when (requirement.structureCondition) {
                    is StructureIdentifierCondition -> (requirement.structureCondition as StructureIdentifierCondition).identifier.toString()
                    is StructureTagCondition -> "#" + (requirement.structureCondition as StructureTagCondition).tag.id.toString()
                    else -> "minecraft:air"
                },
                when (requirement.structureAnticondition) {
                    is StructureIdentifierCondition -> (requirement.structureAnticondition as StructureIdentifierCondition).identifier.toString()
                    is StructureTagCondition -> "#" + (requirement.structureAnticondition as StructureTagCondition).tag.id.toString()
                    else -> "minecraft:air"
                }
            )

            is TimeRangeRequirement -> StringHolder(
                "time_range",
                TimeRange.timeRanges.firstNotNullOfOrNull { (key, value) ->
                    val min = requirement.range.ranges.first().first
                    val max = requirement.range.ranges.first().last
                    if (key != "any" && value.contains(min) && value.contains(max)) {
                        return@firstNotNullOfOrNull key
                    }
                    return@firstNotNullOfOrNull null
                } ?: "any"
            )

            is UseMoveRequirement -> TargetHolder(
                "use_move",
                target = requirement.move.name,
                amount = requirement.amount
            )

            is WeatherRequirement -> WeatherHolder(
                requirement.isRaining,
                requirement.isThundering
            )

            is WorldRequirement -> StringHolder("world", requirement.identifier.toString())


            else -> null
        }
    }
}