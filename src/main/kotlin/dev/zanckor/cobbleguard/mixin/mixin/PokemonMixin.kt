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
import dev.zanckor.cobbleguard.config.SimpleConfig
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
import dev.zanckor.cobbleguard.util.CobbleUtil
import dev.zanckor.cobbleguard.util.Timer
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.SmallFireball
import net.minecraft.world.entity.projectile.Snowball
import net.minecraft.world.entity.projectile.WitherSkull
import net.minecraft.world.entity.projectile.windcharge.WindCharge
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.tslat.smartbrainlib.api.SmartBrainOwner
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.SmartBrainProvider
import net.tslat.smartbrainlib.api.core.behaviour.AllApplicableBehaviours
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import org.jetbrains.annotations.Nullable
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import java.util.*

@Mixin(PokemonEntity::class)
@Debug(export = true, print = true)
class PokemonMixin(
    entityType: EntityType<out PathfinderMob>, level: Level,
    override var isHostile: Boolean,
    override var aggressivity: Aggresivity
) :
    PathfinderMob(entityType, level),
    Hostilemon,
    SmartBrainOwner<PokemonMixin> {

    @Shadow(remap = false)
    @Nullable
    @JvmField
    var pokemon: Pokemon? = null

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
        if (pokemon == null) return null
        if (pokemon!!.moveSet.toList().isEmpty()) return null

        val isPokemon = target is PokemonEntity

        if (isPokemon) {
            val pokemonTarget = target.pokemon
            val moves = mapMoves(pokemon!!.moveSet, pokemonTarget)
                .sortedWith { move1, move2 -> compareMoves(move1, move2) }

            return moves.last().first
        } else {
            return pokemon!!.moveSet[random.nextIntBetweenInclusive(0, pokemon!!.moveSet.count() - 1)]
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
        if (pokemon == null || move == null || target == null) {
            println("CobbleGuard Error: Invalid parameters in useRangedMove. Move: $move, Target: $target, Pokemon: $pokemon")
            return
        }

        updateTargetBehavior(target)
        applyDamageToTarget(target, calculateMoveDamage(move, target))

        CobbleUtil.summonHitParticles(target, CobbleUtil.HIT)
        CobbleUtil.summonHitParticles(target, CobbleUtil.TYPE_HIT(pokemon!!.primaryType))
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
        if (pokemon == null || move == null || target == null) {
            println("CobbleGuard Error: Invalid parameters in useRangedMove. Move: $move, Target: $target, Pokemon: $pokemon")
            return
        }

        val moveType = move.type
        var attackMove = MoveRegistry.getMove(moveType)

        val projectile: Projectile = when (moveType) {
            ElementalTypes.FIRE -> SmallFireball(level(), this, Vec3.ZERO)
            ElementalTypes.ICE, ElementalTypes.WATER, ElementalTypes.BUG, ElementalTypes.GRASS, ElementalTypes.ROCK, ElementalTypes.STEEL, ElementalTypes.GROUND -> WindCharge(
                level(),
                x,
                y,
                z,
                Vec3.ZERO
            )

            ElementalTypes.POISON -> WitherSkull(level(), this, Vec3.ZERO)
            ElementalTypes.ELECTRIC -> {
                val chance = random.nextInt(0, 100)
                attackMove = if (chance < 50) MoveRegistry.ThunderballMove else MoveRegistry.ElectricMove

                Snowball(level(), target.x, target.y + 25, target.z)
            }

            else -> {
                return
            }
        }

        if (projectile !is RangedMove) {
            println("CobbleGuard Error: Generated projectile is not a RangedMove for $moveType")
            return
        }

        // Adjust position only if the move is not electric
        if (projectile.getMove() != MoveRegistry.ElectricMove) {
            projectile.setPos(position().x, position().y + (bbHeight / 2), position().z)
        }

        // Configure and launch the projectile
        projectile.setMove(attackMove)
        projectile.setMoveDamage(calculateMoveDamage(move, target))
        projectile.setOwner(pokemon!!.entity)

        val speed = projectile.getMove()?.speed ?: run {
            return@run 1F
        }

        shootProjectile(projectile, speed, target)
    }


    /**
     * Spawns and shoots a projectile towards the target entity.
     * @param projectile The projectile entity to be spawned
     * @param target The target entity to shoot at
     * @see useRangedMove
     */
    private fun shootProjectile(projectile: Projectile, speed: Float, target: LivingEntity?) {
        val direction = target?.position()?.subtract(projectile.position())?.normalize() ?: return

        projectile.shoot(
            direction.x, direction.y, direction.z,
            0.5f * speed,
            1.0F
        )

        if (!level().isClientSide) {
            level().server?.execute {
                level().addFreshEntity(projectile)
            }
        }
    }

    private fun calculateMoveDamage(move: Move?, target: LivingEntity): Double {
        if (move == null) return 20.0

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
            SimpleConfig.pokemonDamageMultiplier // Standard effectiveness for non-Pokémon entities
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
        if (pokemon == null) return 10.0

        val pokemonLevel = pokemon!!.level
        return (basePower * effectiveness) * (pokemonLevel / 100.0) * 0.4
    }

    /**
     * Applies damage to the target entity.
     *
     * @param target The entity receiving damage
     * @param damage Amount of damage to be applied
     */
    private fun applyDamageToTarget(target: LivingEntity, damage: Double) {
        if (pokemon == null || pokemon!!.entity == null) return

        target.hurt(damageSources().mobAttack(pokemon!!.entity!!), damage.toFloat())
        target.knockback(0.5, position().x - target.position().x, position().z - target.position().z)

        if (target.isDeadOrDying) {
            // In case the target killed is not a PokemonEntity but a Mob, give the XP based on config.
            val isMobEntity = target !is PokemonEntity
            if (isMobEntity) pokemon!!.addExperience(
                SidemodExperienceSource(MODID),
                SimpleConfig.mobXPQuantity
            )

            CobbleUtil.summonEntityParticles(pokemon!!.entity!!, CobbleUtil.WIN_FIGHT, listOf("root"))
        }
    }

    /**
     * Applies damage to the entity and updates the Pokémon's health.
     * @param damageSource The source of the damage
     * @param f The amount of damage to apply
     * @return True if the entity was damaged, false otherwise
     * @see hurt
     */
    override fun hurt(damageSource: DamageSource, f: Float): Boolean {
        if (pokemon == null || pokemon!!.entity == null || pokemon!!.entity!!.isBattling) return false
        val isDamageSourcePokemon = damageSource.entity is PokemonEntity

        val defense = pokemon!!.getStat(Stats.DEFENCE).toFloat() / 300.0
        val configMultiplier = if (isDamageSourcePokemon) 1.0 else SimpleConfig.playerDamageMultiplier
        val damage = f * (1 - defense) * configMultiplier
        val result = super.hurt(damageSource, damage.toFloat())

        pokemon!!.currentHealth = maxOf(0, pokemon!!.currentHealth - damage.toInt()) // Evitar valores negativos

        if (pokemon!!.currentHealth <= 0) {
            pokemon?.entity?.health = 0F
            return super.hurt(damageSource, Float.MAX_VALUE) // Ensure pokemon dies
        } else if (pokemon!!.entity != null) {
            pokemon!!.entity!!.health = pokemon!!.currentHealth.toFloat()
        }

        return result
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
     * - Select move with the highest composite score
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
        if (pokemon == null) return

        val attackerBattle = BattlePokemon.safeCopyOf(attacker)
        attackerBattle.actor = PokemonBattleActor(UUID.randomUUID(), attackerBattle, 10F, RandomBattleAI())

        val pokemonBattle = BattlePokemon.safeCopyOf(pokemon!!)
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
    @Suppress("SENSELESS_COMPARISON")
    override fun interactAt(player: Player, vec3: Vec3, interactionHand: InteractionHand): InteractionResult {
        if (pokemon == null || pokemon!!.getOwnerUUID() != player.uuid) return InteractionResult.FAIL
        if (aggressivity == null) aggressivity = Aggresivity.DEFENSIVE

        val customData = player.mainHandItem.get(DataComponents.CUSTOM_DATA)
        val isGuardItem = customData?.contains("isGuardItem") == true

        if (!player.isShiftKeyDown && interactionHand == InteractionHand.MAIN_HAND && isGuardItem) {
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
        if (pokemon == null) super.knockback(d, e, f)

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
                            "${getUUID()}_defend_task_cooldown", 0.5
                        )
                    },

                WildBehaviourTask()
                    .startCondition { entity ->
                        entity!!.brain.getMemory(NEAREST_WILD_POKEMON_TARGET).isPresent && Timer.hasReached(
                            "${getUUID()}_wild_task_cooldown", 0.5
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