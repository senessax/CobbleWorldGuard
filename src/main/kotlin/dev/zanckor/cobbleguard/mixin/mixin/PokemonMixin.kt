package dev.zanckor.cobbleguard.mixin.mixin

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.pokemon.experience.SidemodExperienceSource
import com.cobblemon.mod.common.api.pokemon.experience.StandardExperienceCalculator
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.ai.RandomBattleAI
import com.cobblemon.mod.common.battles.ai.typeEffectiveness
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import dev.zanckor.cobbleguard.CobbleGuard.Companion.MODID
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_WILD_POKEMON_TARGET
import dev.zanckor.cobbleguard.core.brain.sensor.NearestOwnerTargetSensor
import dev.zanckor.cobbleguard.core.brain.sensor.NearestWildTargetSensor
import dev.zanckor.cobbleguard.core.brain.task.DefendOwnerTask
import dev.zanckor.cobbleguard.core.brain.task.WildBehaviourTask
import dev.zanckor.cobbleguard.core.rangedattacks.MoveRegistry
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon.Aggresivity
import dev.zanckor.cobbleguard.mixin.mixininterface.RangedMove
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.SmallFireball
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.tslat.smartbrainlib.api.SmartBrainOwner
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.SmartBrainProvider
import net.tslat.smartbrainlib.api.core.behaviour.AllApplicableBehaviours
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import java.util.*

@Mixin(PokemonEntity::class)
class PokemonMixin(
    entityType: EntityType<out PathfinderMob>, level: Level,
    override var isHostile: Boolean,
    override var aggressivity: Aggresivity
) :
    PathfinderMob(entityType, level),
    Hostilemon,
    SmartBrainOwner<PokemonMixin> {

    @Shadow
    val pokemon: Pokemon? = null

    private var hostilemonAttacker: PokemonEntity? = null

    /**
     * Selects the most effective move against a target based on type effectiveness and move power
     *
     * Strategy:
     * 1. For Pokémon targets: Analyze move effectiveness and power
     * 2. For non-Pokémon targets: Choose a random move
     *
     * @param target The entity being targeted
     * @return The most suitable move from the Pokémon's moveset
     */
    override fun getBestMoveAgainst(target: LivingEntity?): Move? {
        val isPokemon = target is PokemonEntity

        if (isPokemon) {
            val pokemonTarget = (target as PokemonEntity).pokemon
            val moves = mapMoves(pokemon!!.moveSet, pokemonTarget)
                .sortedWith { move1, move2 -> compareMoves(move1, move2) }

            return moves.last().first
        } else {
            return pokemon!!.moveSet[random.nextIntBetweenInclusive(0, 3)]
        }
    }

    /**
     * Handles the execution of a physical move against a target entity.
     *
     * This method calculates damage based on move power, type effectiveness,
     * and the Pokémon's level, then applies damage to the target.
     *
     * @param move The Move being used
     * @param target The target entity receiving the attack
     */
    override fun usePhysicalMove(move: Move?, target: LivingEntity?) {
        if (move != null && target != null) {
            applyDamageToTarget(target, calculateMoveDamage(move, target))
            updateTargetBehavior(target)

            useRangedMove(move, target)
        }
    }

    /**
     * Handles the execution of a ranged move against a target entity.
     * This method calculates damage based on move power, type effectiveness,
     * and the Pokémon's level, then applies damage to the target.
     * @param move The Move being used
     * @param target The target entity receiving the attack
     * @see usePhysicalMove
     */
    override fun useRangedMove(move: Move?, target: LivingEntity?) {
        if(move != null && target != null) {
            val projectile: Projectile?
            val moveType = move.type

            when(move.type) { // Determine the projectile type based on the move's type
                ElementalTypes.FIRE -> projectile = SmallFireball(level(), this, Vec3.ZERO)
                else -> return
            }

            if(projectile is RangedMove) {
                projectile.setPos(position().x, position().y + eyeHeight, position().z)
                projectile.setMove(MoveRegistry().getMove(ElementalTypes.FIRE)!!)
                projectile.setMoveDamage(calculateMoveDamage(move, target))

                shootProjectile(projectile, projectile.getMove()!!.speed, target)
            }
        }
    }

    /**
     * Spawns and shoots a projectile towards the target entity.
     * @param projectile The projectile entity to be spawned
     * @param target The target entity to shoot at
     * @see useRangedMove
     */
    private fun shootProjectile(projectile : Projectile, speed : Float, target: LivingEntity?) {
        val direction = target!!.position().subtract(0.0, (target.bbHeight / 2).toDouble(), 0.0).subtract(position()).normalize()

        projectile.shoot(
            direction.x, direction.y, direction.z,
            0.5f * speed,
            1.0F)

        level().addFreshEntity(projectile)
    }


    private fun calculateMoveDamage(move: Move, target: LivingEntity): Double {
        val effectiveness = calculateMoveEffectiveness(move, target)
        val basePower = if (move.power == 0.0) 20.0 else move.power

        return calculateDamage(basePower, effectiveness)
    }

    /**
     * Determines move effectiveness based on target's type.
     *
     * @param move The attacking move
     * @param target The target entity
     * @return Effectiveness multiplier (default 1.0 if not a Pokémon)
     */
    private fun calculateMoveEffectiveness(move: Move, target: LivingEntity): Double {
        return if (target is PokemonEntity) {
            getMoveEffectiveness(move, target.pokemon.primaryType)
        } else {
            1.0 // Standard effectiveness for non-Pokémon entities
        }
    }

    /**
     * Calculates damage using a simplified damage formula.
     *
     * @param basePower Base power of the move
     * @param effectiveness Type effectiveness multiplier
     * @return Calculated damage amount
     */
    private fun calculateDamage(basePower: Double, effectiveness: Double): Double {
        val pokemonLevel = pokemon?.level ?: 1
        return (basePower * effectiveness) * (pokemonLevel / 100.0) * 0.4
    }

    /**
     * Applies damage to the target entity.
     *
     * @param target The entity receiving damage
     * @param bruteDamage Amount of damage to be applied
     */
    private fun applyDamageToTarget(target: LivingEntity, bruteDamage: Double) {
        var damage = bruteDamage

        if(target is PokemonEntity) {
            val defense = target.pokemon.getStat(Stats.DEFENCE).toFloat() / 300.0
            damage *= (1 - defense)
        }

        target.hurt(damageSources().mobAttack(this), damage.toFloat())
    }

    /**
     * Updates target's behavior after being attacked.
     *
     * @param target The entity that was attacked
     */
    private fun updateTargetBehavior(target: LivingEntity) {
        if (target is Mob) {
            target.target = this
        }
    }

    override fun getMoveEffectiveness(move: Move?, targetType: ElementalType): Double {
        return typeEffectiveness[move!!.type]?.get(targetType) ?: 0.0
    }

    /**
     * Maps moves to their effectiveness against a target Pokémon
     *
     * Calculates a composite score for each move considering:
     * - Type effectiveness against primary type
     * - Type effectiveness against secondary type (if exists)
     * - Base move power
     *
     * @param moveSet The collection of moves available to the current Pokémon
     * @param pokemonTarget The target Pokémon to evaluate moves against
     * @return A list of moves with their effectiveness scores
     */
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

    /**
     * Compares two moves based on their combined effectiveness and power
     *
     * Scoring method:
     * - Multiply type effectiveness by move power
     * - Select move with highest composite score
     *
     * @param move1 First move to compare
     * @param move2 Second move to compare
     * @return Comparison result indicating relative move strength
     */
    private fun compareMoves(move1: Triple<Move, Double, Double>, move2: Triple<Move, Double, Double>): Int {
        val move1Value = move1.second * move1.third
        val move2Value = move2.second * move2.third

        return move1Value.compareTo(move2Value)
    }

    /**
     * Experience reward mechanism when this Pokémon is defeated
     *
     * Calculates and awards experience to the attacking Pokémon using:
     * - Standard Experience Calculator
     * - Battle simulation with random AI
     *
     * @param attacker The Pokémon that defeated this entity
     */
    private fun givePokemonExperience(attacker: Pokemon) {
        val attackerBattle = BattlePokemon.safeCopyOf(attacker)
        attackerBattle.actor = PokemonBattleActor(UUID.randomUUID(), attackerBattle, 10F, RandomBattleAI())

        val pokemonBattle = BattlePokemon.safeCopyOf(this.pokemon!!)
        pokemonBattle.actor = PokemonBattleActor(UUID.randomUUID(), pokemonBattle, 10F, RandomBattleAI())

        val experienceResult = StandardExperienceCalculator.calculate(attackerBattle, pokemonBattle, 1.0)

        hostilemonAttacker!!.pokemon.addExperience(SidemodExperienceSource(MODID), experienceResult)
    }

    /**
     * Interaction handler for player interactions
     *
     * Toggles Pokémon aggression mode when right-clicked without sneaking
     * Provides feedback to the player about current aggression state
     *
     * @param player The player interacting with the Pokémon
     * @param vec3 Interaction position
     * @param interactionHand The hand used for interaction
     * @return Result of the interaction
     */
    override fun interactAt(player: Player, vec3: Vec3, interactionHand: InteractionHand): InteractionResult {
        @Suppress("SENSELESS_COMPARISON")
        if (aggressivity == null) Aggresivity.DEFENSIVE

        if (!player.isShiftKeyDown && interactionHand == InteractionHand.MAIN_HAND) {
            aggressivity = aggressivity.next()
            player.sendSystemMessage(Component.literal(aggressivity.message))
        }

        return super.interactAt(player, vec3, interactionHand)
    }

    /**
     * Handles Pokémon removal from the world.
     * Awards experience to the attacking Pokémon if killed by a hostilemon.
     */
    override fun remove(removalReason: RemovalReason) {
        if (removalReason == RemovalReason.KILLED && hostilemonAttacker != null) {
            givePokemonExperience(hostilemonAttacker!!.pokemon)
        }

        super.remove(removalReason)
    }

    /**
     * Applies knockback to the entity with a power based on Pokémon attack stat
     */
    override fun knockback(d: Double, e: Double, f: Double) {
        val power = pokemon!!.attack.toFloat() / 75.0

        super.knockback(d * power, e * power, f * power)
    }

    /**
     * Returns the core tasks for the Pokémon's brain
     * @return BrainActivityGroup containing core tasks
     * @see BrainActivityGroup
     */
    override fun getCoreTasks(): BrainActivityGroup<out PokemonMixin> {
        return BrainActivityGroup.coreTasks(
            AllApplicableBehaviours(
                DefendOwnerTask()
                    .startCondition { entity ->
                        entity!!.brain.getMemory(NEAREST_OWNER_TARGET).isPresent && Timer.hasReached(
                            "${getUUID()}_task_cooldown",
                            1
                        )
                    },

                WildBehaviourTask()
                    .startCondition { entity ->
                        entity!!.brain.getMemory(NEAREST_WILD_POKEMON_TARGET).isPresent && Timer.hasReached(
                            "${getUUID()}_task_cooldown",
                            1
                        )
                    }
            )
        )
    }

    /**
     * Returns the sensors for the Pokémon's brain
     * @return List of sensors
     */
    @Suppress("UNCHECKED_CAST")
    override fun getSensors(): MutableList<out ExtendedSensor<out PokemonMixin>> {
        val sensors = mutableListOf<ExtendedSensor<out PokemonMixin>>()

        sensors.add(NearestOwnerTargetSensor() as ExtendedSensor<out PokemonMixin>)
        sensors.add(NearestWildTargetSensor() as ExtendedSensor<out PokemonMixin>)

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