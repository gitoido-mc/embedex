package lol.gito.embedex.config

import com.google.gson.GsonBuilder
import lol.gito.embedex.EmbeDEX
import java.io.File
import java.io.FileReader
import java.io.PrintWriter

/**
 * Code courtesy of https://github.com/timinc-cobble
 *
 * Thanks dude!
 */
class ConfigBuilder<T> private constructor(private val clazz: Class<T>, private val path: String) {
    companion object {
        fun <T> load(clazz: Class<T>, path: String): T {
            return ConfigBuilder(clazz, path)._load()
        }
    }

    fun _load(): T {
        val gson = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

        var config = gson.fromJson("{}" /*default value*/, clazz)
        val configFile = File("config/$path.json")
        configFile.parentFile.mkdirs()

        if (configFile.exists()) {
            try {
                val fileReader = FileReader(configFile)
                config = gson.fromJson(fileReader, clazz)
                fileReader.close()
            } catch (_: Exception) {
                EmbeDEX.LOGGER.error("Error reading config file")
            }
        }

        val pw = PrintWriter(configFile)
        gson.toJson(config, pw)
        pw.close()

        return config
    }
}