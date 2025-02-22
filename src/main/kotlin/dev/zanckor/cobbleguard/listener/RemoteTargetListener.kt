package dev.zanckor.cobbleguard.listener

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.util.MCUtil
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Items.*
import java.util.*

object RemoteTargetListener {
    val playerRemoteTarget = HashMap<UUID, LivingEntity>()

    private fun setRemoteTarget(playerUUID: UUID, target: LivingEntity) {
        playerRemoteTarget[playerUUID] = target
    }

    fun init() {
        registerRemoteTargetAction()
    }

    private fun registerRemoteTargetAction() {
        UseItemCallback.EVENT.register { player, world, hand ->
            if (world.isClientSide) return@register InteractionResultHolder.pass(player.getItemInHand(hand))
            if (player.getItemInHand(hand).`is`(STICK)) {
                MCUtil.getEntityLookingAt(player, 240.0)?.let {
                    if (it !is PokemonEntity ||  it.pokemon.getOwnerUUID() != player.uuid) {
                        setRemoteTarget(player.uuid, it)

                        player.sendSystemMessage(Component.literal("New Target at ${it.blockPosition()}"))
                    }
                }
            }

            return@register InteractionResultHolder.success(player.getItemInHand(hand))
        }
    }
}