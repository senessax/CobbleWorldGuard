package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
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
            entity.getNavigation()
                .moveTo(movePosition.x.toDouble(), movePosition.y.toDouble(), movePosition.z.toDouble(), 1.0)
        }

        return distanceToMove <= (distance * 1.25)
    }
}