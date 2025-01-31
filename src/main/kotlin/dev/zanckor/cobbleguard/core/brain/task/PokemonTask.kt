package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour

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
            val speed = entity.pokemon.getStat(Stats.SPEED).toDouble() / 50.0

            entity.getNavigation().moveTo(movePosition.x.toDouble(), movePosition.y.toDouble(), movePosition.z.toDouble(), speed)
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
        hostilemon.useMove(hostilemon.getBestMoveAgainst(target), target)
        Timer.start("${pokemon.stringUUID}_attack_cooldown", 0.2)
    }
}