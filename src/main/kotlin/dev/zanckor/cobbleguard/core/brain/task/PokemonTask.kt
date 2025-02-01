package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.client.net.animation.PlayPosableAnimationHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.Rotation
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour
import kotlin.math.atan2


abstract class PokemonTask : ExtendedBehaviour<PokemonEntity>() {


    /**
     * Moves the entity to a position. If the entity is more than X blocks away from the position, and the entity is not already moving to the position, move to it
     * @param entity The entity to move
     * @param movePosition The position to move to
     * @param distance In case the entity is more than X blocks away from the position, the entity will move to it, otherwise it won't
     * @return If the entity is near enough to the position
     */
    protected fun moveToPosition(entity: PokemonEntity, movePosition: BlockPos, distance: Double): Boolean {
        val distanceToMove: Double = entity.distanceToSqr(movePosition.center)
        val navigationPosition: BlockPos? = entity.getNavigation().targetPos

        // If the entity is more than X blocks away from the position, and the entity is not already moving to the position, move to it
        if (distanceToMove > distance && (navigationPosition == null || navigationPosition.distSqr(movePosition) > 5.0)) {
            val speed = (entity.pokemon.getStat(Stats.SPEED).toDouble() / 75.0)
            val limitedSpeed = if (speed < 1.0) 1.0 else if (speed > 2.5) 2.5 else speed

            entity.getNavigation()
                .moveTo(movePosition.x.toDouble(), movePosition.y.toDouble(), movePosition.z.toDouble(), limitedSpeed)
        }

        return distanceToMove <= (distance * 1.25)
    }

    /**
     * Attacks the target, using the best move against it.
     * It also starts a cooldown for the attack, so the entity doesn't attack every tick
     * @param pokemon The entity that will attack
     * @param target The entity that will be attacked
     * @see Hostilemon.getBestMoveAgainst
     */
    protected fun attack(pokemon: PokemonEntity, target: LivingEntity) {
        val hostilemon = pokemon as Hostilemon
        val distanceToTarget = pokemon.distanceToSqr(target)
        pokemon.lookAt(target, 30.0f, 30.0f)


        if (distanceToTarget > 25.0) {
            hostilemon.useRangedMove(hostilemon.getBestMoveAgainst(target), target)
        } else {
            Thread {
                Thread.sleep(1200)
                hostilemon.usePhysicalMove(hostilemon.getBestMoveAgainst(target), target)
            }.start()

            if(Timer.hasReached("${pokemon.stringUUID}_attack_animation_cooldown", 2.0)) {
                playSwingAnimation(pokemon)
            }
        }


        Timer.start("${pokemon.stringUUID}_attack_cooldown", 2.0)
    }

    private fun playSwingAnimation(entity: PokemonEntity) {
        if (entity.pokemon.getOwnerPlayer() == null) return
        val pkt = PlayPosableAnimationPacket(
            entity.id,
            setOf("physical"),
            listOf()
        )
        entity.pokemon.getOwnerPlayer()!!.sendPacket(pkt)
    }
}