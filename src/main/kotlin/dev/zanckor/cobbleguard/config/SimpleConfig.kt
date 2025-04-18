package dev.zanckor.cobbleguard.config

import java.io.File
import java.util.Properties

object SimpleConfig {
    var isWildPokesPassive: Boolean = true
    var pokemonDamageMultiplier: Double = 1.0
    var playerDamageMultiplier: Double = 1.0

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
        pokemonDamageMultiplier = props.getProperty("pokemonDamageMultiplier", "1.0").toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 1.0
        playerDamageMultiplier = props.getProperty("playerDamageMultiplier", "1.0").toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 1.0
    }

    fun save() {
        val props = Properties().apply {
            setProperty("isWildPokesPassive", isWildPokesPassive.toString())
            setProperty("pokemonDamageMultiplier", pokemonDamageMultiplier.toString())
            setProperty("playerDamageMultiplier", playerDamageMultiplier.toString())
        }

        configFile.parentFile.mkdirs()
        configFile.outputStream().use { props.store(it, """
            CobbleGuard Config.
            
            isWildPokesPassive - If true, wild pokes won't attack you unless you attack first.
            pokemonDamageMultiplier - Damage multiplier applied to attacks from Pokemons to Players or Mobs. (0.0 -> 10.0)
            playerDamageMultiplier - Damage multiplier applied to attacks from Players or Mobs to Pokemons. (0.0 -> 10.0)
        """.trimIndent()) }
    }
}