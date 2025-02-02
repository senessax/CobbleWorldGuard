package dev.zanckor.cobbleguard.core.brain.sensor

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon.Aggresivity.*
import dev.zanckor.cobbleguard.util.CobbleUtil
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.monster.Monster
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor

@Suppress("SENSELESS_COMPARISON")
class NearestOwnerTargetSensor : ExtendedSensor<LivingEntity>() {
    override fun memoriesUsed(): MutableList<MemoryModuleType<*>> {
        return mutableListOf(NEAREST_OWNER_TARGET)
    }

    override fun type(): SensorType<out ExtendedSensor<*>> {
        return PokemonSensors.NEAREST_OWNER_TARGET
    }

    override fun doTick(level: ServerLevel, entity: LivingEntity) {
        val pokemonEntity = entity as PokemonEntity
        if (pokemonEntity.pokemon.getOwnerUUID() == null) return

        getTarget(entity)?.let {
            if(entity.distanceToSqr(it) > 100.0) return
            pokemonEntity.brain.setMemory(NEAREST_OWNER_TARGET, it)
        }

        super.doTick(level, entity)
    }

    private fun getTarget(entity: LivingEntity): LivingEntity? {
        val pokemonEntity = entity as? PokemonEntity ?: return null
        val target = pokemonEntity.brain.getMemory(NEAREST_OWNER_TARGET).orElse(null)

        if(target != null && target.isAlive && !target.isDeadOrDying) return target

        return getNewTarget(pokemonEntity)
    }

    private fun getNewTarget(pokemonEntity: PokemonEntity): LivingEntity? {
        if((pokemonEntity as Hostilemon).aggressivity == null) pokemonEntity.aggressivity = DEFENSIVE

        return when((pokemonEntity as Hostilemon).aggressivity) {
            STAY, PASSIVE -> null
            HOSTILE -> getHostileTarget(pokemonEntity)
            DEFENSIVE -> getDefensiveTarget(pokemonEntity)
            AGGRESIVE -> getAggressiveTarget(pokemonEntity)
        }
    }

    private fun getDefensiveTarget(entity: PokemonEntity): LivingEntity? {
        val player = entity.pokemon.getOwnerPlayer() ?: return null
        val playerAttacker = player.lastAttacker
        val playerTarget = player.lastHurtMob

        return when {
            playerTarget != null && playerTarget.isAlive -> playerTarget
            playerAttacker != null && playerAttacker.isAlive -> playerAttacker
            else -> null
        }
    }

    private fun getHostileTarget(entity: PokemonEntity): LivingEntity? {
        val defensiveTarget = getDefensiveTarget(entity)
        if(defensiveTarget != null) return defensiveTarget

        val level = entity.level()
        val nearbyEntities = level.getEntities(entity, entity.boundingBox.inflate(15.0)) { it is Monster }

        return nearbyEntities.minByOrNull { entity.distanceToSqr(it) } as? LivingEntity
    }

    private fun getAggressiveTarget(entity: PokemonEntity): LivingEntity? {
        val defensiveTarget = getDefensiveTarget(entity)
        if(defensiveTarget != null) return defensiveTarget

        val level = entity.level()
        val nearbyEntity = level.getEntities(entity, entity.boundingBox.inflate(15.0)) {
            val isOwner = it.uuid.equals(entity.pokemon.getOwnerUUID())
            val isItself = it.uuid.equals(entity.uuid)

            if(isItself || isOwner) return@getEntities false

            if(it is TamableAnimal) {
                return@getEntities it.ownerUUID != entity.pokemon.getOwnerUUID()
            }

            return@getEntities true
        }

        return nearbyEntity.minByOrNull { entity.distanceToSqr(it) } as? LivingEntity
    }
}