package dev.zanckor.cobbleguard.core.brain.task

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.util.CobbleUtil
import dev.zanckor.cobbleguard.util.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour

/**
 * Base class for Pokemon AI tasks implementing common behavior patterns
 */
abstract class PokemonTask : ExtendedBehaviour<PokemonEntity>() {
    companion object {
        private const val MIN_MOVEMENT_SPEED = 1.0
        private const val MAX_MOVEMENT_SPEED = 2.5

        private const val ATTACK_COOLDOWN = 3.0
        private const val ANIMATION_COOLDOWN = 3.0

        private const val MELEE_RANGE = 15.0

        private const val MELEE_ANIMATION_DELAY = 1200L
        private const val RANGED_ATTACK_DELAY = 1000L


        private const val SPECIAL_ANIMATION = "special"
        private const val PHYSICAL_ANIMATION = "physical"
    }

    /**
     * Moves the entity to a specified position with pathfinding
     *
     * @param entity The Pokemon entity to move
     * @param targetPos The position to move to
     * @param minDistance Minimum distance to consider position reached
     * @return True if entity is within acceptable range of target position
     */
    protected fun moveToPosition(
        entity: PokemonEntity,
        targetPos: BlockPos,
        minDistance: Double
    ): Boolean {
        val distanceToTarget = entity.distanceToSqr(targetPos.center)
        val currentNavTarget = entity.navigation.targetPos

        if (shouldUpdateNavigation(distanceToTarget, minDistance, currentNavTarget, targetPos)) {
            val speed = calculateMovementSpeed(entity)
            entity.navigation.moveTo(
                targetPos.x.toDouble(),
                targetPos.y.toDouble(),
                targetPos.z.toDouble(),
                speed
            )
        }

        return distanceToTarget <= (minDistance * 1.25)
    }

    /**
     * Executes an attack against a target using the most effective move
     *
     * @param pokemon The attacking Pokemon entity
     * @param target The target entity
     */
    protected fun attack(pokemon: PokemonEntity, target: LivingEntity) {
        if (!Timer.hasReached("${pokemon.stringUUID}_attack_cooldown", ATTACK_COOLDOWN) || pokemon.isBattling || (target is PokemonEntity && target.isBattling)) {
            return
        }

        val hostilemon = pokemon as? Hostilemon ?: return
        val distanceToTarget = pokemon.distanceToSqr(target)
        CobbleUtil.lookAt(pokemon, target)

        when {
            distanceToTarget > (MELEE_RANGE * 2) -> executeRangedAttack(pokemon, hostilemon, target)
            else -> executeMeleeAttack(pokemon, hostilemon, target)
        }

        Timer.start("${pokemon.stringUUID}_attack_cooldown", ATTACK_COOLDOWN)
    }

    /**
     * Determines if the navigation should be updated
     * @param currentDistance The current distance to the target
     * @param minDistance The minimum distance to consider the target reached
     * @param currentTarget The current target position
     * @param newTarget The new target position
     * @return True if the navigation should be updated
     */
    private fun shouldUpdateNavigation(
        currentDistance: Double,
        minDistance: Double,
        currentTarget: BlockPos?,
        newTarget: BlockPos
    ): Boolean {
        return currentDistance > minDistance &&
                (currentTarget == null || currentTarget.distSqr(newTarget) > 5.0)
    }

    /**
     * Calculates the movement speed of a Pokemon entity
     * @param entity The Pokemon entity
     * @return The movement speed of the entity
     * @see MIN_MOVEMENT_SPEED
     * @see MAX_MOVEMENT_SPEED
     */
    private fun calculateMovementSpeed(entity: PokemonEntity): Double {
        val baseSpeed = entity.pokemon.getStat(Stats.SPEED).toDouble() / 75.0
        return baseSpeed.coerceIn(MIN_MOVEMENT_SPEED, MAX_MOVEMENT_SPEED)
    }

    /**
     * Executes a ranged attack against a target entity
     * @param hostilemon The hostilemon interface
     * @param target The target entity
     */
    private fun executeRangedAttack(
        pokemon: PokemonEntity,
        hostilemon: Hostilemon,
        target: LivingEntity
    ) {
        @Suppress("SENSELESS_COMPARISON")
        if(pokemon == null) return

        val move = hostilemon.getBestMoveAgainst(target)

        CoroutineScope(Dispatchers.IO).launch {
            delay(RANGED_ATTACK_DELAY)
            hostilemon.useRangedMove(move, target)
            playMoveSound(move, pokemon, target)
        }

        if (Timer.hasReached("${pokemon.stringUUID}_attack_animation_cooldown", ANIMATION_COOLDOWN)) {
            CobbleUtil.playAnimation(pokemon, SPECIAL_ANIMATION)
            CobbleUtil.lookAt(pokemon, target)
        }
    }

    /**
     * Executes a melee attack against a target entity
     * @param pokemon The attacking Pokemon entity
     * @param hostilemon The hostilemon interface
     * @param target The target entity
     */
    private fun executeMeleeAttack(
        pokemon: PokemonEntity,
        hostilemon: Hostilemon,
        target: LivingEntity
    ) {
        val move = hostilemon.getBestMoveAgainst(target)
        val pokemonType = pokemon.pokemon.primaryType

        CoroutineScope(Dispatchers.IO).launch {
            delay(MELEE_ANIMATION_DELAY)

            playImpactSound(pokemon, pokemonType)
            hostilemon.usePhysicalMove(move, target)
        }

        if (Timer.hasReached("${pokemon.stringUUID}_attack_animation_cooldown", ANIMATION_COOLDOWN)) {
            CobbleUtil.playAnimation(pokemon, PHYSICAL_ANIMATION)
            CobbleUtil.lookAt(pokemon, target)
        }
    }


    private fun playMoveSound(move: Move?, pokemon: PokemonEntity, target: LivingEntity) {
        val moveName = move?.name?.lowercase() ?: "none"
        val moveSound = CobbleUtil.getSoundByName("move.$moveName.actor") ?: CobblemonSounds.IMPACT_NORMAL

        pokemon.level().playSound(null, target.blockPosition(), moveSound, target.soundSource, 2.0f, 1.0f)
    }

    private fun playImpactSound(pokemon: PokemonEntity, pokemonType: ElementalType) {
        val impactSound = CobbleUtil.getSoundByName("impact.${pokemonType.name.lowercase()}") ?: CobblemonSounds.IMPACT_NORMAL

        pokemon.level().playSound(null, pokemon.blockPosition(), impactSound, pokemon.soundSource, 2.0f, 1.0f)
    }
}