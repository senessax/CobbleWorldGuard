package dev.zanckor.cobbleguard.core.brain.sensor

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor

class NearestHostileMobSensor : ExtendedSensor<LivingEntity>() {
    override fun memoriesUsed(): MutableList<MemoryModuleType<*>> {
        return mutableListOf(MemoryModuleType.NEAREST_HOSTILE)
    }

    override fun type(): SensorType<out ExtendedSensor<*>> {
        return PokemonSensors.NEAREST_TARGET
    }

    override fun doTick(level: ServerLevel, entity: LivingEntity) {
        if(isWildPokemon(entity as PokemonEntity)) {
            return
        }

        val player = entity.pokemon.getOwnerPlayer()
        val playerAttacker = player?.lastAttacker
        val playerTarget = player?.lastHurtMob

        val hasPlayerAttacker = (playerAttacker != null && playerAttacker.isAlive)
        val hasPlayerTarget = (playerTarget != null && playerTarget.isAlive)

        val target = if(hasPlayerTarget) playerTarget else if(hasPlayerAttacker) playerAttacker else null

        if(target != null) {
            entity.brain.setMemory(MemoryModuleType.NEAREST_HOSTILE, target)
        }

        super.doTick(level, entity)
    }

    private fun isWildPokemon(entity: PokemonEntity): Boolean {
        return entity.pokemon.getOwnerPlayer() == null
    }
}