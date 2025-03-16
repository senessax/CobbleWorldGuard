package dev.zanckor.cobbleguard.listener

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.util.CobbleUtil
import dev.zanckor.cobbleguard.util.MCUtil
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult
import java.util.*

object RemoteTargetListener {
    val playerRemoteTarget = HashMap<UUID, LivingEntity?>()

    private fun setRemoteTarget(playerUUID: UUID, target: LivingEntity?) {
        playerRemoteTarget[playerUUID] = target
    }

    fun init() {
        registerRemoteTargetAction()
    }

    private fun registerRemoteTargetAction() {
        UseItemCallback.EVENT.register { player, _, hand ->
            if (!isCorrectItem(player.getItemInHand(hand)) || player.level().isClientSide) {
                return@register InteractionResultHolder.pass(player.getItemInHand(hand))
            }

            // Try to select a target first
            selectTarget(player, hand)?.let { return@register it }

            // If no target selected, try to clear existing target
            remoteTargetOff(player, hand)?.let { return@register it }

            // Default fallback if no other conditions are met
            InteractionResultHolder.pass(player.getItemInHand(hand))
        }
    }

    private fun selectTarget(player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack>? {
        val targetEntity = MCUtil.getEntityLookingAt(player, 480.0) ?: return null

        if (targetEntity !is PokemonEntity || targetEntity.pokemon.getOwnerUUID() != player.uuid && canTarget(targetEntity)) {
            setRemoteTarget(player.uuid, targetEntity)
            player.sendSystemMessage(Component.literal("New Target at ${targetEntity.blockPosition()}"))
            return InteractionResultHolder.success(player.getItemInHand(hand))
        }

        return null
    }


    private fun remoteTargetOff(player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack>? {
        if (player.pick(5.0, 0f, false).type == HitResult.Type.MISS &&
            playerRemoteTarget[player.uuid] != null
        ) {

            setRemoteTarget(player.uuid, null)
            CobbleUtil.removeAllTargets(player as ServerPlayer)

            player.sendSystemMessage(Component.literal("Pokemon Target Cleared"))
            return InteractionResultHolder.success(player.getItemInHand(hand))
        }

        return null
    }

    private fun isCorrectItem(itemStack: ItemStack): Boolean {
        val customData = itemStack.get(DataComponents.CUSTOM_DATA)
        return customData?.contains("isGuardItem") == true // Use /give Player minecraft:stick[minecraft:custom_data={"isGuardItem":true}] to get the item
    }

    private fun canTarget(target: LivingEntity): Boolean {
        return !(CobbleUtil.isPlushie(target) || CobbleUtil.isBoss(target) || CobbleUtil.isPokestop(target))
    }
}