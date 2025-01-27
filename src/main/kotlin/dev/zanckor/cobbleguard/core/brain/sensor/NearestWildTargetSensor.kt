package dev.zanckor.cobbleguard.core.brain.sensor

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_WILD_POKEMON_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor

class NearestWildTargetSensor : ExtendedSensor<LivingEntity>() {
    override fun memoriesUsed(): MutableList<MemoryModuleType<*>> {
        return mutableListOf(NEAREST_WILD_POKEMON_TARGET)
    }

    override fun type(): SensorType<out ExtendedSensor<*>> {
        return PokemonSensors.NEAREST_WILD_POKEMON_TARGET
    }

    override fun doTick(level: ServerLevel, entity: LivingEntity) {
        if(!isWildPokemon(entity as PokemonEntity) || (entity.target != null && entity.target!!.isAlive)) {
            return
        }

        val attacker = entity.lastAttacker
        val hasAttacker = (attacker != null && attacker.isAlive)

        if(hasAttacker) {
            entity.brain.setMemory(NEAREST_WILD_POKEMON_TARGET, attacker)
        }

        super.doTick(level, entity)
    }

    private fun isWildPokemon(entity: PokemonEntity): Boolean {
        return entity.pokemon.getOwnerUUID() == null
    }
}