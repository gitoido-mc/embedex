package lol.gito.embedex

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import kotlinx.io.IOException
import lol.gito.embedex.config.ConfigBuilder
import lol.gito.embedex.config.EmbeDEXConfig
import lol.gito.embedex.web.EmbeDEXApp
import lol.gito.embedex.web.dto.DexAbility
import lol.gito.embedex.web.dto.DexMove
import lol.gito.embedex.web.dto.IdentifierDto
import lol.gito.embedex.web.dto.TagDto
import net.fabricmc.api.ModInitializer
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.ServerSocket

object EmbeDEX : ModInitializer {
    const val MOD_ID = "embedex"
    const val FALLBACK_PORT = 25585

    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    lateinit var config: EmbeDEXConfig
    lateinit var server: Http4kServer
    lateinit var speciesHolder: List<Species>

    val speciesLabelsHolder: MutableMap<String, List<String>> = mutableMapOf()
    val labelsHolder: MutableList<String> = mutableListOf()
    val movesHolder: MutableList<DexMove> = mutableListOf()
    val abilitiesHolder: MutableList<DexAbility> = mutableListOf()
    val biomeHolder: MutableMap<String, TagDto> = mutableMapOf()
    val structureHolder: MutableMap<String, TagDto> = mutableMapOf()

    private val cors = ServerFilters.Cors(
        CorsPolicy(
            originPolicy = OriginPolicy.AllowAll(),
            headers = listOf("Content-Type"),
            methods = listOf(Method.GET)
        )
    )

    override fun onInitialize() {
        LOGGER.info("EmbeDEX initializing..")
        config = ConfigBuilder.load(EmbeDEXConfig::class.java, MOD_ID)
        server = cors.then(EmbeDEXApp()).asServer(
            Undertow(
                port = findPort()
            )
        )

        PokemonSpecies.observable.subscribe(Priority.LOWEST) {
            speciesHolder = PokemonSpecies.implemented.toList()

            val labels = mutableListOf<String>()
            speciesHolder.map { species ->
                val speciesLabels = mutableListOf<String>()

                speciesLabels.addAll(species.labels.toList())

                species.forms.map { form ->
                    speciesLabels.addAll(form.labels.toList())
                }
                speciesLabelsHolder.put(species.resourceIdentifier.path, speciesLabels.distinct())
                labels.addAll(speciesLabels.distinct())
            }

            labelsHolder.addAll(labels.distinct())

            abilitiesHolder.addAll(Abilities.all().map { it ->
                DexAbility(
                    it.name,
                    it.displayName.asTranslated().string,
                    it.description.asTranslated().string,
                )
            })

            movesHolder.addAll(Moves.all().map { it ->
                DexMove(
                    it.name,
                    it.displayName.string,
                    it.description.string,
                    it.power,
                    it.accuracy,
                    it.critRatio,
                    it.maxPp
                )
            })

            LOGGER.info("EmbeDEX received species observable data, ready to start")
        }

        PlatformEvents.SERVER_STARTED.subscribe(Priority.LOWEST) { event ->
            LOGGER.info("Starting EmbeDEX")

            getBiomeData(event.server)
            getStructureData(event.server)

            server.start()
            LOGGER.info("EmbeDEX started on " + server.port())
        }

        PlatformEvents.SERVER_STOPPING.subscribe(Priority.LOWEST) {
            server.stop()
        }
    }


    private fun findPort(): Int {
        if (config.port == 0) {
            return FALLBACK_PORT
        }

        if (config.port > 0) {
            try {
                val socket = ServerSocket(config.port)
                return socket.localPort.also {
                    LOGGER.info("Configured port ${config.port} is available, all good.")
                    socket.close()
                }
            } catch (e: IOException) {
                LOGGER.warn("Configured port ${config.port} could not be bound: $e")
                LOGGER.warn("Falling back to $FALLBACK_PORT.")
                return FALLBACK_PORT
            }
        } else {
            val socket = ServerSocket(0)
            return socket.localPort.also {
                LOGGER.info("New port ${socket.localPort} assigned by ServerSocket.")
                socket.close()
            }
        }
    }

    private fun getBiomeData(server: MinecraftServer) {
        server.registryManager.get(RegistryKeys.BIOME).let { registry ->
            var tags = 0
            registry.streamTagsAndEntries().forEach { tag ->
                val tagId = "#${tag.first.id}"
                val tagName = tag.first.name.string
                val biomes = mutableListOf<IdentifierDto>()

                tag.second.map { biome ->
                    biomes.add(
                        IdentifierDto(
                            biome.key.get().value.toString(),
                            "biome.${biome.key.get().value.toTranslationKey()}".asTranslated().string
                        )
                    )
                }

                biomeHolder.put(tagId, TagDto(tagId, tagName, biomes))
                tags += 1
            }
        }
    }

    private fun getStructureData(server: MinecraftServer) {
        server.registryManager.get(RegistryKeys.STRUCTURE).let { registry ->
            var tags = 0
            registry.streamTagsAndEntries().forEach { tag ->
                val tagId = "#${tag.first.id}"

                structureHolder.put(tagId, TagDto(
                    id = tagId,
                    items = tag.second.map { structure ->
                        IdentifierDto(structure.key.get().value.toString())
                    }
                ))

                tags += 1
            }
            LOGGER.info("Loaded $tags structure tags")
        }
    }
}