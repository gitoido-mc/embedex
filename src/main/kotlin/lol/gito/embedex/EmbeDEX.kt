package lol.gito.embedex

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import lol.gito.embedex.web.EmbeDEXApp
import lol.gito.embedex.web.dto.dex.Classifier
import net.fabricmc.api.ModInitializer
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BiomeTags
import net.minecraft.registry.tag.TagKey
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

object EmbeDEX : ModInitializer {
    const val MOD_ID = "embedex"
    lateinit var server: Http4kServer
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    lateinit var speciesHolder: List<Species>
    lateinit var labelsHolder: List<String>
    lateinit var abilitiesHolder: List<Classifier>
    lateinit var movesHolder: List<Classifier>

    private val cors = ServerFilters.Cors(
        CorsPolicy(
            originPolicy = OriginPolicy.AllowAll(),
            headers = listOf("Content-Type"),
            methods = listOf(Method.GET)
        )
    )

    override fun onInitialize() {
        LOGGER.info("EmbeDEX initializing..")

        server = cors.then(EmbeDEXApp()).asServer(Undertow(25585))

        PokemonSpecies.observable.subscribe(Priority.LOWEST) {
            speciesHolder = PokemonSpecies.implemented.toList()

            val labels = mutableListOf<String>()
            speciesHolder.map { species ->
                labels.addAll(species.labels.toList())
                species.forms.map { form ->
                    labels.addAll(form.labels.toList())
                }
            }

            labelsHolder = labels.distinct()

            abilitiesHolder = Abilities.all().map { it ->
                Classifier(
                    it.name,
                    it.displayName.asTranslated().string,
                    it.description.asTranslated().string
                )
            }
            movesHolder = Moves.all().map { it ->
                Classifier(
                    it.name,
                    it.displayName.string,
                    it.description.string
                )
            }
            LOGGER.info("EmbeDEX received species observable data, ready to start")
        }

        PlatformEvents.SERVER_STARTED.subscribe(Priority.LOWEST) { event ->
            LOGGER.info("Starting EmbeDEX")

            server.start()

            LOGGER.info("EmbeDEX started on " + server.port())
        }

        PlatformEvents.SERVER_STOPPING.subscribe(Priority.LOWEST) {
            server.stop()
        }
    }
}