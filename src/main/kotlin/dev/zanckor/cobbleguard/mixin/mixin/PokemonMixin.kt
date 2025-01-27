package dev.zanckor.cobbleguard.mixin.mixin

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.battles.ai.typeEffectiveness
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.core.brain.sensor.NearestOwnerTargetSensor
import dev.zanckor.cobbleguard.core.brain.task.DefendOwnerTask
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.Brain
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

    override fun hurt(damageSource: DamageSource, damage: Float): Boolean {
        if(pokemon != null) {
            pokemon.currentHealth -= damage.toInt()
            health = pokemon.currentHealth.toFloat()
        }

        return super.hurt(damageSource, damage)
    }

    override fun getBestMoveAgainst(target: LivingEntity?): Move? {
        val isPokemon = target is PokemonEntity

        if(isPokemon) {
            val pokemonTarget = (target as PokemonEntity).pokemon
            val moves = mapMoves(pokemon!!.moveSet, pokemonTarget)
                .sortedWith { move1, move2 -> compareMoves(move1, move2) }

            return moves.last().first
        } else {
            return pokemon!!.moveSet[random.nextIntBetweenInclusive(0, 3)]
        }
    }

    private fun mapMoves(moveSet: MoveSet, pokemonTarget: Pokemon): List<Triple<Move, Double, Double>> {
        val mappedMoves = mutableListOf<Triple<Move, Double, Double>>()

        for (move in moveSet) {
            val primaryTypeEffectiveness = getMoveEffectiveness(move, pokemonTarget.primaryType)
            val secondaryTypeEffectiveness = pokemonTarget.secondaryType?.let {
                getMoveEffectiveness(move, it)
            } ?: 0.0

            mappedMoves.add(Triple(move, primaryTypeEffectiveness + secondaryTypeEffectiveness, move.power))
        }

        return mappedMoves
    }

    private fun compareMoves(move1: Triple<Move, Double, Double>, move2: Triple<Move, Double, Double>): Int {
        val move1Value = move1.second * move1.third
        val move2Value = move2.second * move2.third

        return move1Value.compareTo(move2Value)
    }

    override fun useMove(move: Move?, target: PathfinderMob?) {
        if(move != null && target != null) {
            val effectiveness = if(target is PokemonEntity) getMoveEffectiveness(move, target.pokemon.primaryType) else 1.0
            val power = if(move.power == 0.0) 20.0 else move.power

            val level = pokemon!!.level
            val damage = (power * effectiveness) * (level / 100.0)

            target.target = this
            target.hurt(damageSources().mobAttack(this), damage.toFloat())
        }
    }

    override fun getMoveEffectiveness(move: Move?, targetType: ElementalType): Double {
        return typeEffectiveness[move!!.type]?.get(targetType) ?: 0.0
    }

    override fun getCoreTasks(): BrainActivityGroup<out PokemonMixin> {
        return BrainActivityGroup.coreTasks(
            DefendOwnerTask()
                .startCondition { entity -> entity!!.brain.getMemory(NEAREST_OWNER_TARGET).isPresent }
                .stopIf { entity -> entity!!.brain.getMemory(NEAREST_OWNER_TARGET).get().isDeadOrDying }
                .whenStopping { entity -> entity!!.brain.eraseMemory(NEAREST_OWNER_TARGET) }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun getSensors(): MutableList<out ExtendedSensor<out PokemonMixin>> {
        val sensors = mutableListOf<ExtendedSensor<out PokemonMixin>>()

        sensors.add(NearestOwnerTargetSensor() as ExtendedSensor<out PokemonMixin>)

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