package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.util.Pair
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour

class AttackTask : ExtendedBehaviour<PokemonEntity>() {
    override fun getMemoryRequirements(): MutableList<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return mutableListOf(Pair(MemoryModuleType.NEAREST_HOSTILE, MemoryStatus.VALUE_PRESENT))
    }

    override fun start(pokemon: PokemonEntity?) {
        pokemon!!.target = pokemon.brain.getMemory(MemoryModuleType.NEAREST_HOSTILE).get()
        pokemon.moveTo(pokemon.target!!.blockPosition().center)

        super.start(pokemon)
    }
}