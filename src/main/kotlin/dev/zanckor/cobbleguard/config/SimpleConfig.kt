package dev.zanckor.cobbleguard.config

import java.io.File
import java.util.Properties

object SimpleConfig {
    var isWildPokesPassive: Boolean = true
    var damageMultiplier: Double = 1.0

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
        damageMultiplier = props.getProperty("damageMultiplier", "1.0").toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 1.0
    }

    fun save() {
        val props = Properties().apply {
            setProperty("isWildPokesPassive", isWildPokesPassive.toString())
            setProperty("damageMultiplier", damageMultiplier.toString())
        }

        configFile.parentFile.mkdirs()
        configFile.outputStream().use { props.store(it, """
            CobbleGuard Config.
            
            isWildPokesPassive - If true, wild pokes won't attack you unless you attack first.
            damageMultiplier - Damage multiplier (From 0.0 to 10.0) to non-pokemon entities as players.
        """.trimIndent()) }
    }
}