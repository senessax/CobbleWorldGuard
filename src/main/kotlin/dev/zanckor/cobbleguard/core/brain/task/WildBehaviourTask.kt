package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.util.Pair
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_WILD_POKEMON_TARGET
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.util.CobbleUtil
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

class WildBehaviourTask : PokemonTask() {
    override fun getMemoryRequirements(): MutableList<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return mutableListOf(Pair(NEAREST_WILD_POKEMON_TARGET, MemoryStatus.VALUE_PRESENT))
    }

    override fun start(pokemon: PokemonEntity?) {
        val canAttack = Timer.hasReached("${pokemon?.stringUUID}_attack_cooldown", true)
        val target = pokemon?.brain?.getMemory(NEAREST_WILD_POKEMON_TARGET)?.get()
        if(!canAttack || pokemon == null || target == null || target.isDeadOrDying) {
            return
        }

        val isNearEnough = moveToPosition(pokemon, target.blockPosition(), 10.0)

        if(target is PokemonEntity) {
            println(CobbleUtil.mustRunAwayPokemon(pokemon, target))
        }

        if(isNearEnough) {
            attack(pokemon, target)
        }

        pokemon.target = target
        super.start(pokemon)
    }

    private fun attack(pokemon: PokemonEntity, target: LivingEntity) {
        val hostilemon = pokemon as Hostilemon
        hostilemon.useMove(hostilemon.getBestMoveAgainst(target), target)
        Timer.start("${pokemon.stringUUID}_attack_cooldown", 0.6)
    }
}