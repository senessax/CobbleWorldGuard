package dev.zanckor.cobbleguard.mixin.mixin

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.battles.ai.typeEffectiveness
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import dev.zanckor.cobbleguard.core.brain.sensor.NearestHostileMobSensor
import dev.zanckor.cobbleguard.core.brain.task.AttackTask
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.level.Level
import net.tslat.smartbrainlib.api.SmartBrainOwner
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.SmartBrainProvider
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow

@Mixin(PokemonEntity::class)
class PokemonMixin(entityType: EntityType<out PathfinderMob>, level: Level,
                   override var isHostile: Boolean
) :
    PathfinderMob(entityType, level),
    Hostilemon,
    SmartBrainOwner<PokemonMixin> {

    @Shadow
    val pokemon: Pokemon? = null
    override fun getBestMoveAgainst(target: LivingEntity?): Move? {
        val isPokemon = target is PokemonEntity

        if(isPokemon) {
            val pokemonTarget = (target as PokemonEntity).pokemon
            val moves = pokemon!!.moveSet.map {
                val primaryTypeEffectiveness = getMoveEffectiveness(it, pokemonTarget.primaryType)
                val secondaryTypeEffectiveness = if(pokemonTarget.secondaryType != null) getMoveEffectiveness(it, pokemonTarget.secondaryType!!) else 0.0

                Triple(it, primaryTypeEffectiveness + secondaryTypeEffectiveness, it.power)
            }.sortedBy { it.second + it.third }

            return moves.last().first
        } else {
            return pokemon!!.moveSet[random.nextIntBetweenInclusive(0, 3)]
        }
    }

    override fun useMove(move: Move?, target: LivingEntity?) {
        if(move != null) {
            val damage = move.power * getMoveEffectiveness(move, (target as PokemonEntity).pokemon.primaryType)
            target.hurt(damageSources().mobAttack(this), damage.toFloat())
        }
    }

    override fun getMoveEffectiveness(move: Move?, targetType: ElementalType): Double {
        val type = move!!.type

        typeEffectiveness[type]?.let {
            return it[targetType] ?: 0.0
        }

        return 0.0
    }

    override fun getCoreTasks(): BrainActivityGroup<out PokemonMixin> {
        return BrainActivityGroup.coreTasks(
            AttackTask()
                .startCondition { entity -> entity!!.brain.getMemory(MemoryModuleType.NEAREST_HOSTILE).isPresent }
                .stopIf { entity -> entity!!.brain.getMemory(MemoryModuleType.NEAREST_HOSTILE).isEmpty }
                .whenStopping { entity -> entity!!.brain.eraseMemory(MemoryModuleType.NEAREST_HOSTILE) }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun getSensors(): MutableList<out ExtendedSensor<out PokemonMixin>> {
        val sensors = mutableListOf<ExtendedSensor<out PokemonMixin>>()

        sensors.add(NearestHostileMobSensor() as ExtendedSensor<out PokemonMixin>)

        return sensors
    }

    @Suppress("KotlinConstantConditions")
    override fun brainProvider(): Brain.Provider<*> {
        return SmartBrainProvider(this) as Brain.Provider<*>
    }

    override fun customServerAiStep() {
        tickBrain(this)
    }
}