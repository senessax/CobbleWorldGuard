package dev.zanckor.cobbleguard.core.brain.sensor

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import dev.zanckor.cobbleguard.listener.RemoteTargetListener
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon.Aggresivity.*
import dev.zanckor.cobbleguard.util.CobbleUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
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
        if (!CobbleUtil.isWorldAllowed(level)) return

        val pokemonEntity = entity as PokemonEntity
        if (pokemonEntity.pokemon.getOwnerUUID() == null) return

        getTarget(entity)?.let {
            if (entity.distanceToSqr(it) > 100.0) return
            pokemonEntity.brain.setMemory(NEAREST_OWNER_TARGET, it)
        }

        super.doTick(level, entity)
    }

    private fun getTarget(entity: LivingEntity): LivingEntity? {
        val pokemonEntity = entity as? PokemonEntity ?: return null
        val target = pokemonEntity.brain.getMemory(NEAREST_OWNER_TARGET).orElse(null)

        if (target != null && target.isAlive && !target.isDeadOrDying) {
            return target
        }

        return pokemonEntity.lastAttacker ?: getNewTarget(pokemonEntity)
    }

    private fun getNewTarget(pokemonEntity: PokemonEntity): LivingEntity? {
        if ((pokemonEntity as Hostilemon).aggressivity == null) pokemonEntity.aggressivity = DEFENSIVE
        val aggresivity = (pokemonEntity as Hostilemon).aggressivity
        val remoteTarget = RemoteTargetListener.playerRemoteTarget[pokemonEntity.pokemon.getOwnerUUID()]

        if (aggresivity == STAY || aggresivity == PASSIVE) return null

        val target = when ((pokemonEntity as Hostilemon).aggressivity) {
            STAY, PASSIVE -> null
            HOSTILE -> getHostileTarget(pokemonEntity)
            DEFENSIVE -> getDefensiveTarget(pokemonEntity)
            AGGRESIVE -> getAggressiveTarget(pokemonEntity)
        }

        if(target != null) {
            if(target is PokemonEntity) {
                if(target.pokemon.getOwnerUUID() == pokemonEntity.pokemon.getOwnerUUID()) return null
            }

            return target
        }

        if (remoteTarget != null && remoteTarget.isAlive && remoteTarget.distanceToSqr(pokemonEntity) < 240.0) {
            return reasignRemoteTarget(pokemonEntity)
        }

        return null
    }

    private fun reasignRemoteTarget(pokemonEntity: PokemonEntity): LivingEntity? {
        val remoteTarget = RemoteTargetListener.playerRemoteTarget[pokemonEntity.pokemon.getOwnerUUID()]
        if (remoteTarget != null && remoteTarget.isAlive) {
            pokemonEntity.target = remoteTarget

            pokemonEntity.navigation.moveTo(
                remoteTarget.x,
                remoteTarget.y,
                remoteTarget.z,
                1.5
            )

            return remoteTarget
        }

        return null
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
        if (defensiveTarget != null) return defensiveTarget

        val level = entity.level()
        val nearbyEntities = level.getEntities(entity, entity.boundingBox.inflate(15.0)) { it is Monster }

        return nearbyEntities.minByOrNull { entity.distanceToSqr(it) } as? LivingEntity
    }

    private fun getAggressiveTarget(entity: PokemonEntity): LivingEntity? {
        if(entity.lastAttacker != null && entity.lastAttacker!!.isAlive) return entity.lastAttacker

        val level = entity.level()
        val nearbyEntity = level.getEntities(entity, entity.boundingBox.inflate(15.0)) { target ->
            canAttack(entity, target)
        }

        return nearbyEntity.minByOrNull { entity.distanceToSqr(it) } as? LivingEntity
    }

    private fun canAttack(entity: PokemonEntity, target: Entity): Boolean {
        if (CobbleUtil.isPlushie(target) || CobbleUtil.isBoss(target) || CobbleUtil.isPokestop(target)) return false
        if (CobbleUtil.isPlushie(entity) || CobbleUtil.isBoss(entity) || CobbleUtil.isPokestop(entity)) return false
        if (target.uuid == entity.uuid) return false
        if (target.uuid == entity.pokemon.getOwnerUUID()) return false

        if (target is TamableAnimal && target.ownerUUID == entity.uuid) return false
        if (target is Player && (target.isCreative || target.isSpectator)) return false

        return true
    }
}