package dev.zanckor.cobbleguard

import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import net.fabricmc.api.ModInitializer

class CobbleGuard : ModInitializer {

    override fun onInitialize() {
        PokemonSensors.init()
        PokemonMemoryModuleType.init()
    }

    companion object {
        @JvmField
        var MODID: String = "cobbleguard"
    }
}
