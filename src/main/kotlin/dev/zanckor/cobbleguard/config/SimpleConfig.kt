package dev.zanckor.cobbleguard.config

import org.yaml.snakeyaml.Yaml
import java.io.File

object SimpleConfig {
    enum class DimensionFilter {
        WHITELIST, BLACKLIST, ALL;

        companion object {
            fun from(input: String?): DimensionFilter {
                return entries.firstOrNull { it.name.equals(input?.trim(), ignoreCase = true) }
                    ?: run {
                        println("Warning: Unknown dimensionFilter value \"$input\". Falling back to BLACKLIST.")
                        BLACKLIST
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
    var dimensionFilter: DimensionFilter = DimensionFilter.ALL

    // Optional feature values (not yet implemented)
    var pokeNatureCheck: Boolean = true
    var pokeNatureChances: Map<String, Double> = emptyMap()

    private val configFile = File("config/cobbleguard/config.yml")

    private fun configInitilized() {
        val yamlContent = """
        # CobbleGuard Config

        # If true, wild Pokémon won't attack on sight unless provoked.
        isWildPokesPassive: true

        # XP gained by Pokémon when killing standard Minecraft mobs.
        mobXPQuantity: 1

        # Speed multiplier applied to Pokémon when fleeing.
        runAwaySpeedMultiplier: 1.0

        # Multiplier for damage dealt by Pokémon to players/mobs.
        pokemonDamageMultiplier: 1.0

        # Multiplier for damage dealt by players/mobs to Pokémon.
        playerDamageMultiplier: 1.0

        # Allowed dimensions where wild Pokémon will flee or attack.
        allowedDimensions:
          - minecraft:overworld
          - minecraft:the_nether
          - minecraft:the_end

        # Filter mode for the above dimensions:
        #   WHITELIST - Only listed dimensions allow behavior
        #   BLACKLIST - Listed dimensions disallow behavior
        #   ALL       - All dimensions allowed (ignores allowedDimensions)
        dimensionFilter: ALL

        # NOT YET IMPLEMENTED
        # If true, it decides the chance of attacking or fleeing. 
        # Multiplier: 1 = attack, 0 = flee
        pokeNatureCheck: true
        pokeNatureChances:
          # Neutrals
          - hardy: 0.5
          - docile: 0.5
          - bashful: 0.5
          - quirky: 0.5
          - serious: 0.5
          # Stat Changes
          - lonely: 0.3
          - adamant: 0.8
          - naughty: 1
          - brave: 0.7
          - bold: 0.6
          - impish: 0.2
          - lax: 0.3
          - relaxed: 0.3
          - modest: 0.5
          - mild: 0.4
          - rash: 0.6
          - quiet: 0.2
          - calm: 0.3
          - gentle: 0.1
          - careful: 0.4
          - sassy: 0.6
          - timid: 0.2
          - hasty: 0.7
          - jolly: 0.0
          - naive: 0.2
    """.trimIndent()

        configFile.writeText(yamlContent)
    }

    fun load() {
        configFile.parentFile.mkdirs() // Ensure folder exists

        if (!configFile.exists()) {
            println("CobbleGuard config.yml not found, generating now!")
            configInitilized()
            return
        }

        val yaml = Yaml()
        val config: Map<String, Any> = configFile.inputStream().use {
            yaml.load(it) ?: emptyMap()
        }

        isWildPokesPassive = config["isWildPokesPassive"] as? Boolean ?: isWildPokesPassive
        mobXPQuantity = (config["mobXPQuantity"] as? Int) ?: mobXPQuantity
        runAwaySpeedMultiplier = (config["runAwaySpeedMultiplier"] as? Double) ?: runAwaySpeedMultiplier
        pokemonDamageMultiplier = (config["pokemonDamageMultiplier"] as? Double)?.coerceIn(0.0, 10.0) ?: pokemonDamageMultiplier
        playerDamageMultiplier = (config["playerDamageMultiplier"] as? Double)?.coerceIn(0.0, 10.0) ?: playerDamageMultiplier
        dimensionFilter = DimensionFilter.from(config["dimensionFilter"] as? String)

        @Suppress("UNCHECKED_CAST")
        allowedDimensions = config["allowedDimensions"] as? List<String> ?: allowedDimensions

        pokeNatureCheck = config["pokeNatureCheck"] as? Boolean ?: pokeNatureCheck

        @Suppress("UNCHECKED_CAST")
        val rawChances = config["pokeNatureChances"] as? List<Map<String, Double>>
        pokeNatureChances = rawChances?.associate { it.entries.first().toPair() } ?: emptyMap()

        println("CobbleGuard config loaded.")
    }

    fun reload() {
        load()
        println("CobbleGuard config reloaded.")
    }
}
