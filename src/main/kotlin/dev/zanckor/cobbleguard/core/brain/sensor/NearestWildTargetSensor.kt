package dev.zanckor.cobbleguard.core.brain.sensor

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_WILD_POKEMON_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import dev.zanckor.cobbleguard.util.CobbleUtil
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
        val pokemonEntity = entity as? PokemonEntity ?: return
        if (pokemonEntity.pokemon.getOwnerUUID() != null) return

        val attacker = pokemonEntity.lastAttacker
        if (attacker?.isAlive == true) {
            pokemonEntity.brain.setMemory(NEAREST_WILD_POKEMON_TARGET, attacker)
        }

        super.doTick(level, entity)
    }
}