package dev.zanckor.cobbleguard.core.brain.sensor

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor

class NearestOwnerTargetSensor : ExtendedSensor<LivingEntity>() {
    override fun memoriesUsed(): MutableList<MemoryModuleType<*>> {
        return mutableListOf(NEAREST_OWNER_TARGET)
    }

    override fun type(): SensorType<out ExtendedSensor<*>> {
        return PokemonSensors.NEAREST_OWNER_TARGET
    }

    override fun doTick(level: ServerLevel, entity: LivingEntity) {
        val pokemonEntity = entity as? PokemonEntity ?: return
        if (pokemonEntity.pokemon.getOwnerUUID() == null) return

        val player = entity.pokemon.getOwnerPlayer() ?: return
        val playerAttacker = player.lastAttacker
        val playerTarget = player.lastHurtMob

        val target = when {
            playerTarget != null && playerTarget.isAlive -> playerTarget
            playerAttacker != null && playerAttacker.isAlive -> playerAttacker
            else -> null
        }

        target?.let {
            if(entity.distanceToSqr(it) > 100.0) return
            pokemonEntity.brain.setMemory(NEAREST_OWNER_TARGET, it)
        }

        super.doTick(level, entity)
    }
}