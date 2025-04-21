package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.util.Pair
import dev.zanckor.cobbleguard.config.SimpleConfig
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_WILD_POKEMON_TARGET
import dev.zanckor.cobbleguard.util.CobbleUtil
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

class WildBehaviourTask : PokemonTask() {
    override fun getMemoryRequirements(): MutableList<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return mutableListOf(Pair(NEAREST_WILD_POKEMON_TARGET, MemoryStatus.VALUE_PRESENT))
    }

    override fun start(pokemon: PokemonEntity?) {
        val canAttack = Timer.hasReached("${pokemon?.stringUUID}_wild_attack_cooldown", true)
        val target = pokemon?.brain?.getMemory(NEAREST_WILD_POKEMON_TARGET)?.get()

        if (!canAttack || target == null || target.isDeadOrDying || pokemon.distanceToSqr(target) > 200) {
            customStop(pokemon!!)
            return
        }

        pokemon.target = target

        // Run away if the target is a Pokemon and the Pokemon is stronger
        if (CobbleUtil.mustRunAway(pokemon, target)) {
            runAway(pokemon)
        } else {
            // Otherwise, attack the target
            moveToPosition(pokemon, target.blockPosition(), pokemon.boundingBox.size * 10)
            attack(pokemon, target)
        }

        super.start(pokemon)
    }

    private fun runAway(pokemon: PokemonEntity) {
        val target = pokemon.brain.getMemory(NEAREST_WILD_POKEMON_TARGET).get()
        val direction = pokemon.position().subtract(target.position()).normalize()
        val runAwayPosition = pokemon.position().add(direction.x * 10, 0.0, direction.z * 10)
        val speedModifier = 1.6 * SimpleConfig.runAwaySpeedMultiplier

        pokemon.navigation.moveTo(runAwayPosition.x, runAwayPosition.y, runAwayPosition.z, speedModifier)
    }

    private fun customStop(pokemon: PokemonEntity) {
        pokemon.navigation.stop()
        pokemon.brain.clearMemories()
    }
}