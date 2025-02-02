package dev.zanckor.cobbleguard.core.brain.task

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
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour

/**
 * Base class for Pokemon AI tasks implementing common behavior patterns
 */
abstract class PokemonTask : ExtendedBehaviour<PokemonEntity>() {
    companion object {
        private const val MIN_MOVEMENT_SPEED = 1.0
        private const val MAX_MOVEMENT_SPEED = 2.5
        private const val ATTACK_COOLDOWN = 2.0
        private const val ANIMATION_COOLDOWN = 2.0
        private const val MELEE_RANGE = 25.0
        private const val MELEE_ANIMATION_DELAY = 1200L
        private const val SPECIAL_ANIMATION_COOLDOWN = 1500L

        private const val SPECIAL_ANIMATION = "special"
        private const val PHYSICAL_ANIMATION = "physical"

        private val HIT = ResourceLocation.fromNamespaceAndPath("cobblemon", "hit")
        private val PHYCHIC = ResourceLocation.fromNamespaceAndPath("cobblemon", "psychic_impactdots")
        private val ON_FIRE = ResourceLocation.fromNamespaceAndPath("cobblemon", "flamethrower_target_linger")

        private fun TYPE_HIT(elementalType: ElementalType) = ResourceLocation.fromNamespaceAndPath(
            "cobblemon",
            "impact_${elementalType.name.lowercase()}"
        )
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
        if (!Timer.hasReached("${pokemon.stringUUID}_attack_cooldown", ATTACK_COOLDOWN)) {
            return
        }

        val hostilemon = pokemon as? Hostilemon ?: return
        val distanceToTarget = pokemon.distanceToSqr(target)
        CobbleUtil.lookAt(pokemon, target)

        when {
            distanceToTarget > MELEE_RANGE -> executeRangedAttack(pokemon, hostilemon, target)
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
        CoroutineScope(Dispatchers.IO).launch {
            delay(SPECIAL_ANIMATION_COOLDOWN)
            summonHitParticles(pokemon, target, HIT)
            summonHitParticles(pokemon, target, TYPE_HIT(pokemon.pokemon.primaryType))

            hostilemon.useRangedMove(hostilemon.getBestMoveAgainst(target), target)
            CobbleUtil.lookAt(pokemon, target)
        }

        if (Timer.hasReached("${pokemon.stringUUID}_attack_animation_cooldown", ANIMATION_COOLDOWN)) {
            CobbleUtil.playAnimation(pokemon, SPECIAL_ANIMATION)
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
        CoroutineScope(Dispatchers.IO).launch {
            delay(MELEE_ANIMATION_DELAY)
            summonHitParticles(pokemon, target, HIT)
            summonHitParticles(pokemon, target, TYPE_HIT(pokemon.pokemon.primaryType))

            hostilemon.usePhysicalMove(hostilemon.getBestMoveAgainst(target), target)
            CobbleUtil.lookAt(pokemon, target)
        }

        if (Timer.hasReached("${pokemon.stringUUID}_attack_animation_cooldown", ANIMATION_COOLDOWN)) {
            CobbleUtil.playAnimation(pokemon, PHYSICAL_ANIMATION)
        }
    }

    /**
     * Summons hit particles at the target entity's position
     * @param entity The Pokemon entity that is attacking
     * @param target The entity that is being attacked
     * @param resourceLocation The resource location of the particle
     */
    private fun summonHitParticles(
        entity: PokemonEntity,
        target: LivingEntity,
        resourceLocation: ResourceLocation
    ) {
        val targetPosition = target.position().add(0.0, target.eyeHeight.toDouble(), 0.0)
        CobbleUtil.sendBedrockParticle(entity, targetPosition, resourceLocation)
    }
}