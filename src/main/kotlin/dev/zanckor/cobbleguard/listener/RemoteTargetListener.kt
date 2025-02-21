package dev.zanckor.cobbleguard.listener

import dev.zanckor.cobbleguard.util.MCUtil
import net.fabricmc.fabric.api.event.player.UseItemCallback
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
                MCUtil.getEntityLookingAt(player, 20.0)?.let { setRemoteTarget(player.uuid, it) }
            }

            return@register InteractionResultHolder.success(player.getItemInHand(hand))
        }
    }
}