package dev.zanckor.cobbleguard.listener

import com.cobblemon.mod.common.api.events.CobblemonEvents
import net.minecraft.world.entity.player.Player

object CapturePokemonListener {

    fun init() {
        registerCaptureActionEvent()
    }

    private fun registerCaptureActionEvent() {
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { event ->
            val player = event.pokeBall.owner

            if (player != null && player is Player) {
                player.attack(event.pokemon)
            }
        }
    }
}