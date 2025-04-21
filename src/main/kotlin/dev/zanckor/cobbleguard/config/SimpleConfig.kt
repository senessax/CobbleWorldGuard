package dev.zanckor.cobbleguard.config

import java.io.File
import java.util.Properties

object SimpleConfig {
    var isWildPokesPassive: Boolean = true
    var mobXPQuantity: Int = 1
    var runAwaySpeedMultiplier: Double = 1.0
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
        mobXPQuantity = props.getProperty("mobXPQuantity", "20").toInt()
        runAwaySpeedMultiplier = props.getProperty("runAwaySpeedMultiplier", "1.0").toDoubleOrNull() ?: 1.0
        pokemonDamageMultiplier = props.getProperty("pokemonDamageMultiplier", "1.0").toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 1.0
        playerDamageMultiplier = props.getProperty("playerDamageMultiplier", "1.0").toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 1.0
    }

    fun save() {
        val props = Properties().apply {
            setProperty("isWildPokesPassive", isWildPokesPassive.toString())
            setProperty("mobXPQuantity", mobXPQuantity.toString())
            setProperty("pokemonDamageMultiplier", pokemonDamageMultiplier.toString())
            setProperty("playerDamageMultiplier", playerDamageMultiplier.toString())
            setProperty("runAwaySpeedMultiplier", runAwaySpeedMultiplier.toString())
        }

        configFile.parentFile.mkdirs()
        configFile.outputStream().use { props.store(it, """
            CobbleGuard Config.
            
            isWildPokesPassive - If true, wild pokes won't attack you unless you attack first.
            mobXPQuantity - On kill a normal Minecraft Mob, how much XP Pokémon should receive.
            runAwaySpeedMultiplier - Speed multiplier applied to Pokémon on run-away.
            pokemonDamageMultiplier - Damage multiplier applied to attacks from Pokémon to Players or Mobs. (0.0 -> 10.0)
            playerDamageMultiplier - Damage multiplier applied to attacks from Players or Mobs to Pokémon. (0.0 -> 10.0)
        """.trimIndent()) }
    }
}