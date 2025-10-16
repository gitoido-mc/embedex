package lol.gito.embedex.web.dto.dex.spawn

import com.cobblemon.mod.common.api.spawning.MoonPhaseRange
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.registry.BiomeIdentifierCondition
import com.cobblemon.mod.common.registry.BiomeTagCondition
import lol.gito.embedex.EmbeDEX.LOGGER

data class SpawnHolder(
    val id: String,
    val type: String? = null,
    val context: String? = null,
    val bucket: String,
    val level: LevelRange? = null,
    val weight: Float,
    val condition: Map<String, Any>
) {
    data class LevelRange(val min: Int? = null, val max: Int? = null)

    companion object {
        fun fromSpawningCondition(value: SpawningCondition<*>): Map<String, Any> {
            val result: MutableMap<String, Any> = mutableMapOf()

            value.dimensions?.map { it.toString() }?.let {
                result.put("dimensions", it)
            }

            value.biomes?.toMutableSet()?.map { it ->
                when (it) {
                    is BiomeIdentifierCondition -> it.identifier.toString()
                    is BiomeTagCondition -> "#" + it.tag.id.toString()
                    else -> null
                }
            }?.let {
                result.put("biomes", it.filterNotNull())
            }

            value.structures?.map { it ->
                if (it.right().isEmpty) return@map it.left().get().toString()
                if (it.left().isEmpty) return@map "#" + it.right().get().id.toString()
                return@map null
            }?.let {
                result.put("structures", it.filterNotNull())
            }

            value.moonPhase?.let { phase ->
                MoonPhaseRange.moonPhaseRanges.firstNotNullOfOrNull { (key, range) ->
                    val min = phase.ranges.first().first
                    val max = phase.ranges.first().first
                    if (range.contains(min) && range.contains(max)) {
                        return@firstNotNullOfOrNull key
                    }
                    return@firstNotNullOfOrNull null
                }?.let { result.put("moonPhase", it) }
            }

            value.isThundering?.let { result.put("isThundering", value.isThundering!!) }
            value.isRaining?.let { result.put("isRaining", value.isRaining!!) }
            value.isSlimeChunk?.let { result.put("isSlimeChunk", value.isSlimeChunk!!) }
            value.minX?.let { result.put("minX", it) }
            value.maxX?.let { result.put("maxX", it) }
            value.minY?.let { result.put("minY", it) }
            value.maxY?.let { result.put("maxY", it) }
            value.minZ?.let { result.put("minZ", it) }
            value.maxZ?.let { result.put("maxZ", it) }
            value.minSkyLight?.let { result.put("minSkyLight", it) }
            value.maxSkyLight?.let { result.put("maxSkyLight", it) }
            value.minLight?.let { result.put("minLight", it) }
            value.maxLight?.let { result.put("maxLight", it) }
            value.canSeeSky?.let { result.put("canSeeSky", it) }

            return result
        }

        fun fromSpawnDetail(detail: SpawnDetail): SpawnHolder {
            var type: String? = null
            var context: String? = null
            var level: LevelRange? = null
            val condition: MutableMap<String, Any> = mutableMapOf()
            when (detail) {
                is PokemonSpawnDetail -> {
                    type = detail.type
                    context = detail.context.name
                    level = LevelRange(detail.getDerivedLevelRange().min(), detail.getDerivedLevelRange().max())
                    detail.conditions.forEach { value ->
                        condition.putAll(fromSpawningCondition(value))
                    }
                }

                else -> {
                    LOGGER.info("hitting other case")
                }
            }


            return SpawnHolder(
                id = detail.id,
                type = type,
                context = context,
                bucket = detail.bucket.name,
                level = level,
                weight = detail.bucket.weight,
                condition = condition
            )
        }
    }
}
