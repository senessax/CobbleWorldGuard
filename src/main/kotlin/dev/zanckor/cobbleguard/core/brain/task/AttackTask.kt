package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.util.Pair
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_HOSTILE
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour

class AttackTask : ExtendedBehaviour<PokemonEntity>() {
    override fun getMemoryRequirements(): MutableList<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return mutableListOf(Pair(NEAREST_HOSTILE, MemoryStatus.VALUE_PRESENT))
    }

    override fun start(pokemon: PokemonEntity?) {
        val target = pokemon?.brain?.getMemory(NEAREST_HOSTILE)?.get()
        if(pokemon == null || target == null || target.isDeadOrDying) {
            return
        }

        val isNearEnough = moveToPosition(pokemon, target.blockPosition(), 10.0)
        pokemon.target = target

        if(isNearEnough && Timer.hasReached("${pokemon.stringUUID}_attack_cooldown", true) && target is PathfinderMob) {
            val hostilemon = pokemon as Hostilemon
            hostilemon.useMove(hostilemon.getBestMoveAgainst(target), target)

            Timer.start("${pokemon.stringUUID}_attack_cooldown", 0.6)
        }

        super.start(pokemon)
    }

    private fun moveToPosition(entity: PokemonEntity, movePosition: BlockPos, distance: Double): Boolean {
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