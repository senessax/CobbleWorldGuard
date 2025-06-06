package dev.zanckor.cobbleguard.config

import java.io.File
import java.util.*

object SimpleConfig {
    enum class DimensionFilter {
        WHITELIST, BLACKLIST, ALL;

        companion object {
            fun from(input: String): DimensionFilter {
                val normalized = input.trim().uppercase()

                return when (normalized) {
                    "WHITELIST" -> WHITELIST
                    "BLACKLIST" -> BLACKLIST
                    "ALL" -> ALL
                    else -> {
                        // Log error™ & default for user having fat fingers
                        println("Warning: Unknown dimensionFilter value \"$input\". Falling back to BLACKLIST.")
                        BLACKLIST
                    }
                }
            }
        }
    }

    var isWildPokesPassive: Boolean = true
    var mobXPQuantity: Int = 1
    var runAwaySpeedMultiplier: Double = 1.0
    var pokemonDamageMultiplier: Double = 1.0
    var playerDamageMultiplier: Double = 1.0
    var allowedDimensions: List<String> = listOf(
        "minecraft:overworld",
        "minecraft:the_nether",
        "minecraft:the_end"
    )
    var dimensionFilter = DimensionFilter.WHITELIST

    private val configFile = File("config/cobbleguard.cfg")

    fun load() {
        if (!configFile.exists()) {
            save()
            return
        }

        val props = Properties().apply {
            load(configFile.inputStream())
        }

        isWildPokesPassive = props.getProperty("isWildPokesPassive", "false").toBoolean()
        mobXPQuantity = props.getProperty("mobXPQuantity", "20").toInt()
        runAwaySpeedMultiplier = props.getProperty("runAwaySpeedMultiplier", "1.0").toDoubleOrNull() ?: 1.0
        pokemonDamageMultiplier = props.getProperty("pokemonDamageMultiplier", "1.0").toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 1.0
        playerDamageMultiplier = props.getProperty("playerDamageMultiplier", "1.0").toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 1.0

        val rawDimensions = props.getProperty("allowedDimensions", "")
        val parsedDimensions = rawDimensions.split(",").map { it.trim() }
        allowedDimensions = parsedDimensions.filter { it.isNotEmpty() }

        dimensionFilter = DimensionFilter.from(props.getProperty("dimensionFilter", "WHITELIST"))
    }

    fun reload() {
        load()
        println("CobbleGuard config reloaded.")
    }

    fun save() {
        val props = Properties().apply {
            setProperty("isWildPokesPassive", isWildPokesPassive.toString())
            setProperty("mobXPQuantity", mobXPQuantity.toString())
            setProperty("pokemonDamageMultiplier", pokemonDamageMultiplier.toString())
            setProperty("playerDamageMultiplier", playerDamageMultiplier.toString())
            setProperty("runAwaySpeedMultiplier", runAwaySpeedMultiplier.toString())
            setProperty("dimensionFilter", dimensionFilter.name)
            // I despise "\:" escapes, it is so ugly. Possible use of Yaml later?
            setProperty("allowedDimensions", allowedDimensions.joinToString(","))
        }

        configFile.parentFile.mkdirs()
        configFile.outputStream().use {
            props.store(it, """
                CobbleGuard Config.
                
                isWildPokesPassive - If true, wild pokes won't attack you unless you attack first.
                mobXPQuantity - On kill a normal Minecraft Mob, how much XP Pokémon should receive.
                runAwaySpeedMultiplier - Speed multiplier applied to Pokémon on run-away.
                pokemonDamageMultiplier - Damage multiplier applied to attacks from Pokémon to Players or Mobs. (0.0 -> 10.0)
                playerDamageMultiplier - Damage multiplier applied to attacks from Players or Mobs to Pokémon. (0.0 -> 10.0)
                allowedDimensions - Comma-separated list of allowed dimension IDs (e.g., minecraft:overworld)
                dimensionFilterMode - Designates what filter the allowedDimensions uses. "WHITELIST", "BLACKLIST", or "ALL". ALL skips the dimension check.
                
            """.trimIndent())
        }
    }
}