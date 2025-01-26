package dev.zanckor.cobbleguard

import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import net.fabricmc.api.ModInitializer

class CobbleGuard : ModInitializer {

    override fun onInitialize() {
        PokemonSensors.init()
    }

    companion object {
        @JvmField
        var MODID: String = "cobbleguard"
    }
}
