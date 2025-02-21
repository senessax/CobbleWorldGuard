package dev.zanckor.cobbleguard.util

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.LivingEntity
import kotlin.math.atan2
import kotlin.math.max

object CobbleUtil {

    val HIT = ResourceLocation.fromNamespaceAndPath("cobblemon", "hit")
    val FLAMETHROWER = ResourceLocation.fromNamespaceAndPath("cobblemon", "flamethrower_actor")
    val BUBBLEBEAM = ResourceLocation.fromNamespaceAndPath("cobblemon", "bubblebeam_actor")
    val HYPNOSIS = ResourceLocation.fromNamespaceAndPath("cobblemon", "hypnosis_actor")
    val POISONPOWDER = ResourceLocation.fromNamespaceAndPath("cobblemon", "poisonpowder")

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
        return if(attackerEntity is PokemonEntity) {
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

        if(attackerCombatStats > pokemonCombatStats) {
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

        for(stat in combatStats) {
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

        for(stat in defenseStats) {
            pokemonDefenseStats += pokemonEntity.pokemon.getStat(stat)
        }

        return pokemonDefenseStats
    }

    /**
     * Sends a bedrock particle to nearby players
     * @param livingEntity The entity at where the particle will be summoned
     * @param particle The particle to be sent
     */
    private fun sendBedrockEntityParticle(livingEntity: LivingEntity, particle: ResourceLocation) {
        val nearbyPlayers = livingEntity.level().players().filter { it.distanceTo(livingEntity) < 1000 }

        nearbyPlayers.forEach {
            (it as ServerPlayer).sendPacket(
                SpawnSnowstormEntityParticlePacket(
                    particle,
                    livingEntity.id,
                    listOf("target"))
            )
        }
    }

    private fun sendBedrockParticle(livingEntity: LivingEntity, particle: ResourceLocation) {
        val nearbyPlayers = livingEntity.level().players().filter { it.distanceTo(livingEntity) < 1000 }
        val lastAttackerHeight = livingEntity.lastHurtByMob?.bbHeight?.div(2)?.let { max(it, livingEntity.bbHeight) } ?: livingEntity.eyeHeight

        nearbyPlayers.forEach {
            (it as ServerPlayer).sendPacket(
                SpawnSnowstormParticlePacket(
                    particle,
                    livingEntity.position().add(0.0, lastAttackerHeight.toDouble(), 0.0),)
            )
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
     * Summons ranged particles at the entity's position
     * @param entity The Pokemon entity that is attacking
     * @param resourceLocation The resource location of the particle
     * @see sendBedrockEntityParticle
     */
    fun summonRangedParticles(
        entity: LivingEntity,
        resourceLocation: ResourceLocation
    ) {
        sendBedrockEntityParticle(entity, resourceLocation)
    }
}