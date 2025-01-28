package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.util.Pair
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

class DefendOwnerTask : PokemonTask() {
    override fun getMemoryRequirements(): MutableList<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return mutableListOf(Pair(NEAREST_OWNER_TARGET, MemoryStatus.VALUE_PRESENT))
    }

    override fun start(pokemon: PokemonEntity?) {
        val canAttack = Timer.hasReached("${pokemon?.stringUUID}_attack_cooldown", true)
        val target = pokemon?.brain?.getMemory(NEAREST_OWNER_TARGET)?.get()
        if(!canAttack || target == null || target.isDeadOrDying || pokemon.distanceToSqr(target) > 200) {
            customStop(pokemon!!)
            return
        }


        pokemon.target = target

        if(moveToPosition(pokemon, target.blockPosition(), pokemon.boundingBox.size * 10)) {
            attack(pokemon, target)
        }

        super.start(pokemon)
    }

    private fun customStop(pokemon: PokemonEntity) {
        pokemon.navigation.stop()
        pokemon.brain.clearMemories()
    }
}