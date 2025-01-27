package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.util.Pair
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

class DefendOwnerTask : PokemonTask() {
    override fun getMemoryRequirements(): MutableList<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return mutableListOf(Pair(NEAREST_OWNER_TARGET, MemoryStatus.VALUE_PRESENT))
    }

    override fun start(pokemon: PokemonEntity?) {
        val target = pokemon?.brain?.getMemory(NEAREST_OWNER_TARGET)?.get()
        if(pokemon == null || target == null || target.isDeadOrDying) {
            return
        }

        val isNearEnough = moveToPosition(pokemon, target.blockPosition(), 10.0)
        val canAttack = Timer.hasReached("${pokemon.stringUUID}_attack_cooldown", true)
        val isPathfinderMob = target is PathfinderMob
        pokemon.target = target

        if(isNearEnough && canAttack && isPathfinderMob) {
            attack(pokemon, target as PathfinderMob)
        }

        super.start(pokemon)
    }

    private fun attack(pokemon: PokemonEntity, target: PathfinderMob) {
        val hostilemon = pokemon as Hostilemon
        hostilemon.useMove(hostilemon.getBestMoveAgainst(target), target)
        Timer.start("${pokemon.stringUUID}_attack_cooldown", 0.6)
    }
}