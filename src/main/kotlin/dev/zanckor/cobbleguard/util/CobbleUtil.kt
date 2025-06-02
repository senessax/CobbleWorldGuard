package dev.zanckor.cobbleguard.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_OWNER_TARGET
import dev.zanckor.cobbleguard.core.brain.registry.PokemonMemoryModuleType.NEAREST_WILD_POKEMON_TARGET
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon.Aggresivity.AGGRESIVE
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.tslat.smartbrainlib.util.BrainUtils
import kotlin.math.atan2
import kotlin.math.max

object CobbleUtil {

    val HIT = ResourceLocation.fromNamespaceAndPath("cobblemon", "hit")

    val FLAMETHROWER = ResourceLocation.fromNamespaceAndPath("cobblemon", "flamethrower_actor")
    val FLAMETHROWER_TARGET = ResourceLocation.fromNamespaceAndPath("cobblemon", "flamethrower_target_linger")

    val WATER_IMPACT = ResourceLocation.fromNamespaceAndPath("cobblemon", "impact_water")

    val POISONPOWDER = ResourceLocation.fromNamespaceAndPath("cobblemon", "poisonpowder")

    val ELECTRICIMPACT = ResourceLocation.fromNamespaceAndPath("cobblemon", "impact_electric")

    val SANDATTACK = ResourceLocation.fromNamespaceAndPath("cobblemon", "sandattack_actor")
    val SANDATTACK_RESIDUAL = ResourceLocation.fromNamespaceAndPath("cobblemon", "sandattack_residual")
    val SANDATTACK_IMPACT = ResourceLocation.fromNamespaceAndPath("cobblemon", "sandattack_impact")

    val ROCK = ResourceLocation.fromNamespaceAndPath("cobblemon", "seismictoss_targetrocks")
    val ROCKIMPACT = ResourceLocation.fromNamespaceAndPath("cobblemon", "crunch_targetrocks")

    val BUGFIRE = ResourceLocation.fromNamespaceAndPath("cobblemon", "ember_fire")
    val BUGFIRETRAIL = ResourceLocation.fromNamespaceAndPath("cobblemon", "ember_firesparks")
    val BUGIMPACT = ResourceLocation.fromNamespaceAndPath("cobblemon", "impact_bug")

    val WIN_FIGHT = ResourceLocation.fromNamespaceAndPath("cobblemon", "statup_actor")

    fun TYPE_HIT(elementalType: ElementalType) = ResourceLocation.fromNamespaceAndPath(
        "cobblemon",
        "impact_${elementalType.name.lowercase()}"
    )

    /**
     * Determines if a Pokemon must run away from another entity if attacked
     * In case the attacker is a Pokemon, it will call mustRunAwayPokemon
     * Otherwise, it will check if the Pokemon's health is lower than the attacker's health
     *
     * @param pokemonEntity The Pokemon that is being attacked
     * @param attackerEntity The entity that is attacking
     * @return If the Pokemon must run away
     * @see mustRunAwayPokemon
     * @see calculateCombatStats
     */
    fun mustRunAway(pokemonEntity: PokemonEntity, attackerEntity: LivingEntity): Boolean {
        val aggresivity = (pokemonEntity as Hostilemon).aggressivity

        return if (aggresivity == AGGRESIVE) {
            false
        } else if (attackerEntity is PokemonEntity) {
            mustRunAwayPokemon(pokemonEntity, attackerEntity)
        } else {
            attackerEntity.health > pokemonEntity.pokemon.currentHealth
        }
    }


    /**
     * Determines if a Pokemon must run away from another Pokemon if attacked
     * @param pokemonEntity The Pokemon that is being attacked
     * @param attackerPokemonEntity The Pokemon that is attacking
     */
    private fun mustRunAwayPokemon(pokemonEntity: PokemonEntity, attackerPokemonEntity: PokemonEntity): Boolean {
        val attackerCombatStats = calculateCombatStats(attackerPokemonEntity)

        val pokemonCombatStats = calculateCombatStats(pokemonEntity)
        val pokemonDefenseStats = calculateDefenseStats(pokemonEntity)

        if (attackerCombatStats > pokemonCombatStats) {
            val mustTryToCombat = attackerCombatStats - pokemonCombatStats > pokemonDefenseStats

            return !mustTryToCombat
        }

        return false
    }

    /**
     * Calculates the combat stats of a Pokemon
     * @param pokemonEntity The Pokemon to calculate the combat stats
     * @return The sum of the combat stats: Attack; Special Attack
     */
    private fun calculateCombatStats(pokemonEntity: PokemonEntity): Int {
        val combatStats = setOf(Stats.ATTACK, Stats.SPECIAL_ATTACK)
        var pokemonCombatStats = 0

        for (stat in combatStats) {
            pokemonCombatStats += pokemonEntity.pokemon.getStat(stat)
        }

        return pokemonCombatStats
    }

    /**
     * Calculates the defense stats of a Pokemon
     * @param pokemonEntity The Pokemon to calculate the defense stats
     * @return The sum of the defense stats: HP; Defense; Special Defense
     */
    private fun calculateDefenseStats(pokemonEntity: PokemonEntity): Int {
        val defenseStats = setOf(Stats.HP, Stats.DEFENCE, Stats.SPECIAL_DEFENCE)
        var pokemonDefenseStats = 0

        for (stat in defenseStats) {
            pokemonDefenseStats += pokemonEntity.pokemon.getStat(stat)
        }

        return pokemonDefenseStats
    }

    /**
     * Sends a bedrock particle to nearby players
     * @param entity The entity at where the particle will be summoned
     * @param particle The particle to be sent
     */
    private fun sendBedrockEntityParticle(
        entity: Entity,
        particle: ResourceLocation,
        locator: List<String> = listOf("target")
    ) {
        val nearbyPlayers = entity.level().players().filter { it.distanceTo(entity) < 1000 }

        nearbyPlayers.forEach {
            if (it is ServerPlayer) {
                it.sendPacket(
                    SpawnSnowstormEntityParticlePacket(
                        particle,
                        entity.id,
                        locator
                    )
                )
            }
        }
    }


    private fun sendBedrockParticle(livingEntity: LivingEntity, particle: ResourceLocation) {
        val nearbyPlayers = livingEntity.level().players().filter { it.distanceTo(livingEntity) < 1000 }
        val lastAttackerHeight = livingEntity.lastHurtByMob?.bbHeight?.div(2)?.let { max(it, livingEntity.bbHeight) }
            ?: livingEntity.eyeHeight

        nearbyPlayers.forEach {
            if (it is ServerPlayer) {
                it.sendPacket(
                    SpawnSnowstormParticlePacket(
                        particle,
                        livingEntity.position().add(0.0, lastAttackerHeight.toDouble(), 0.0),
                    )
                )
            }
        }
    }

    private fun sendBedrockParticle(level: Level, pos: Vec3, particle: ResourceLocation) {
        val nearbyPlayers = level.players().filter { it.distanceToSqr(pos) < 1000 }

        nearbyPlayers.forEach {
            if (it is ServerPlayer) {
                it.sendPacket(
                    SpawnSnowstormParticlePacket(
                        particle,
                        pos
                    )
                )
            }
        }
    }

    /**
     * Makes a Pokemon look at a target entity
     * @param pokemon The Pokemon entity that will look at the target
     * @param target The target entity to look at
     * @see ClientboundRotateHeadPacket
     */
    fun lookAt(pokemon: PokemonEntity, target: LivingEntity) {
        val deltaX = target.x - pokemon.x
        val deltaZ = target.z - pokemon.z

        val yaw = Math.toDegrees(atan2(-deltaX, deltaZ)).toFloat()

        pokemon.yRot = yaw
        pokemon.yHeadRot = yaw

        if (pokemon.level() is ServerLevel) {
            val headRotationPacket = ClientboundRotateHeadPacket(pokemon, (yaw * 256.0f / 360.0f).toInt().toByte())
            (pokemon.level() as ServerLevel).players().forEach { player ->
                player.connection.send(headRotationPacket)
            }
        }
    }


    /**
     * Plays a swing animation for the attacking Pokemon
     * @param entity The Pokemon entity that is attacking
     */
    fun playAnimation(entity: PokemonEntity, animation: String) {
        entity.pokemon.getOwnerPlayer()?.let { player ->
            val animationPacket = PlayPosableAnimationPacket(
                entity.id,
                setOf(animation),
                emptyList()
            )
            player.sendPacket(animationPacket)
        }
    }

    fun getSoundByName(soundName: String): SoundEvent? {
        CobblemonSounds.all().filter { it.location.toString().contains(soundName) }.forEach {
            return it
        }

        return null
    }

    /**
     * Summons hit particles at the target entity's position
     * @param target The Pokemon entity that is being attacked
     * @param resourceLocation The resource location of the particle
     */
    fun summonHitParticles(
        target: LivingEntity,
        resourceLocation: ResourceLocation
    ) {
        sendBedrockParticle(target, resourceLocation)
    }

    /**
     * Summons hit particles at the target entity's position
     * @param level The level where the particles will be summoned
     * @param pos The position where the particles will be summoned
     * @param resourceLocation The resource location of the particle
     */
    fun summonHitParticles(
        level: Level,
        pos: Vec3,
        resourceLocation: ResourceLocation
    ) {
        sendBedrockParticle(level, pos, resourceLocation)
    }

    /**
     * Summons particles at the entity's position
     * @param entity The Pokemon entity that is attacking
     * @param resourceLocation The resource location of the particle
     * @see sendBedrockEntityParticle
     */
    fun summonEntityParticles(
        entity: Entity,
        resourceLocation: ResourceLocation,
        locator: List<String> = listOf("target")
    ) {
        sendBedrockEntityParticle(entity, resourceLocation, locator)
    }

    fun removeAllTargets(player: ServerPlayer) {
        Cobblemon.storage.getParty(player).forEach {
            it.entity?.let { entity ->
                entity.target = null
                BrainUtils.clearMemory(entity, NEAREST_OWNER_TARGET)
            }
        }
    }

    fun isBoss(entity: Entity): Boolean {
        return entity.name.string.contains("Boss") ||
                entity.customName?.string?.contains("Boss") == true ||
                entity.displayName?.string?.contains("Boss") == true
    }

    fun isPlushie(entity: Entity): Boolean {
        try {
            val entityData = CompoundTag()
            entity.save(entityData)

            return entityData.contains("plushie")
        } catch (e: IllegalStateException) {
            return true
        }

        return false
    }

    fun isPokestop(entity: Entity): Boolean {
        val entityData = CompoundTag()
        entity.save(entityData)

        return entityData.contains("pokestop") || entityData.contains("type_pokestop")
    }

    fun isWorldAllowed(level: ServerLevel): Boolean {
        val dimId = level.dimension().location().toString()
        return dev.zanckor.cobbleguard.config.SimpleConfig.allowedDimensions.contains(dimId)
    }
}