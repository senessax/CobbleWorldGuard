package dev.zanckor.cobbleguard.core.brain.sensor

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.config.SimpleConfig
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_WILD_POKEMON_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonSensors
import dev.zanckor.cobbleguard.listener.RemoteTargetListener
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon.Aggresivity.*
import dev.zanckor.cobbleguard.util.CobbleUtil
import net.minecraft.commands.Commands
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
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

        getTarget(entity)?.let {
            if (entity.distanceToSqr(it) > 200.0) return

            pokemonEntity.brain.setMemory(NEAREST_WILD_POKEMON_TARGET, it)
        }

        super.doTick(level, entity)
    }

    private fun getTarget(entity: LivingEntity): LivingEntity? {
        val pokemonEntity = entity as? PokemonEntity ?: return null
        val target = pokemonEntity.brain.getMemory(NEAREST_WILD_POKEMON_TARGET).orElse(null)

        if (target != null && target.isAlive && !target.isDeadOrDying) {
            return target
        }

        return pokemonEntity.lastAttacker ?: getNewTarget(pokemonEntity)
    }

    private fun getNewTarget(pokemonEntity: PokemonEntity): LivingEntity? {
        val aggresivity = assignAggresivity(pokemonEntity)

        if (aggresivity == AGGRESIVE) {
            return getAggressiveTarget(pokemonEntity)
        }

        return null
    }

    private fun getAggressiveTarget(entity: PokemonEntity): LivingEntity? {
        if (entity.lastAttacker != null && entity.lastAttacker!!.isAlive) return entity.lastAttacker

        val level = entity.level()
        val nearbyEntity = level.getEntities(entity, entity.boundingBox.inflate(15.0)) { target ->
            canAttack(entity, target)
        }

        return nearbyEntity.minByOrNull { entity.distanceToSqr(it) } as? LivingEntity
    }

    private fun assignAggresivity(pokemonEntity: PokemonEntity): Hostilemon.Aggresivity {
        val attackStat = pokemonEntity.pokemon.getStat(Stats.ATTACK)

        if ((pokemonEntity as Hostilemon).aggressivity == null) {
            val isPassiveConfig = SimpleConfig.isWildPokesPassive
            val isAggresiveByStat = attackStat > 150

            pokemonEntity.aggressivity =
                if (!isPassiveConfig && isAggresiveByStat) {
                    AGGRESIVE
                } else {
                    DEFENSIVE
                }
        }

        return pokemonEntity.aggressivity
    }

    private fun canAttack(entity: LivingEntity, target: Entity): Boolean {
        if (CobbleUtil.isPlushie(target) || CobbleUtil.isBoss(target) || CobbleUtil.isPokestop(target)) return false
        if (CobbleUtil.isPlushie(entity) || CobbleUtil.isBoss(entity) || CobbleUtil.isPokestop(entity)) return false
        if (target.uuid == entity.uuid) return false
        if (target.uuid == entity.uuid) return false

        if (target is TamableAnimal && target.ownerUUID == entity.uuid) return false
        if (target is Player && (target.isCreative || target.isSpectator)) return false

        return true
    }
}