package lol.gito.embedex.web

import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.pokemon.Species
import com.google.gson.GsonBuilder
import lol.gito.embedex.EmbeDEX
import lol.gito.embedex.EmbeDEX.LOGGER
import lol.gito.embedex.EmbeDEX.labelsHolder
import lol.gito.embedex.EmbeDEX.speciesHolder
import lol.gito.embedex.web.dto.dex.Classifier
import lol.gito.embedex.web.dto.dex.DexDetail
import lol.gito.embedex.web.dto.dex.DexList
import net.minecraft.util.Identifier
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.ConfigurableGson
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

private object Gson: ConfigurableGson(
    GsonBuilder()
        .setPrettyPrinting()
        .asConfigurable()
        .withStandardMappings()
        .done()
)

fun Identifier.toDataUrl() = EmbeDEX::class.java
    .getResource(String.format("/data/%s/%s", namespace, path))
    ?.toURI()
    ?.toURL()

@Suppress("FunctionName")
fun EmbeDEXApp(): RoutingHttpHandler {
    val pageQuery = Query.int().optional("page")
    val perPageQuery = Query.int().optional("perPage")
    val searchQuery = Query.string().optional("search")
    val labelsQuery = Query.string().multi.optional("labels[]")
    val typesQuery = Query.string().multi.optional("types[]")
    val gson = Gson

    return routes(
        "/dex" bind Method.GET to { request ->
            val page: Int = pageQuery(request) ?: 1
            val perPage: Int = perPageQuery(request) ?: 40
            val search: String? = searchQuery(request)
            val labels: List<String>? = labelsQuery(request)
            val types: List<String>? = typesQuery(request)

            val chunkedSpecies = speciesHolder.toList()
                .filter { entry ->
                    var matchedLabels = true
                    var matchedTypes = true
                    if (labels != null) {
                        matchedLabels = entry.labels.containsAll(labels)
                    }
                    if (types != null) {
                        val entryTypes = mutableListOf<String>()
                        entryTypes.add(entry.primaryType.name)
                        if (entry.secondaryType != null ) {
                            entryTypes.add(entry.secondaryType!!.name)
                        }
                        matchedTypes = types.any { entryTypes.contains(it) }
                    }

                    return@filter matchedLabels && matchedTypes
                }
                .sortedBy { it.nationalPokedexNumber }
                .chunked(perPage)

            Response(OK).with(
                gson.autoBody<DexList>().toLens() of DexList.fromSpeciesList(
                    chunkedSpecies.getOrElse(page - 1) { emptyList() },
                    labelsHolder,
                    ElementalTypes.all().map { it -> it.name },
                    search,
                    perPage,
                    page,
                    chunkedSpecies.sumOf { it -> it.count() },
                    chunkedSpecies.count()
                )
            )
        },
        "/dex/{species:\\w+}" bind Method.GET to { request ->
            LOGGER.info(request.path("species")!!)
            val species = speciesHolder.firstOrNull { record ->
                record.resourceIdentifier.path == request.path("species")!!
            }

            when (species == null) {
                true -> Response(NOT_FOUND)
                false -> Response(OK).with(
                    gson.autoBody<DexDetail>().toLens() of DexDetail.fromSpecies(species)
                )
            }
        },
        "/abilities" bind Method.GET to { request ->
            Response(OK).with(
                gson.autoBody<List<Classifier>>().toLens() of EmbeDEX.abilitiesHolder
            )

        },
        "/moves" bind Method.GET to { request ->
            Response(OK).with(
                gson.autoBody<List<Classifier>>().toLens() of EmbeDEX.movesHolder
            )

        }
    )
}