package dev.zanckor.cobbleguard.util

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2

object CobbleUtil {

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
    fun mustRunAwayPokemon(pokemonEntity: PokemonEntity, attackerPokemonEntity: PokemonEntity): Boolean {
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
     * @param livingEntity The entity that will send the particle
     * @param position The position where the particle will be sent
     * @param particle The particle to be sent
     */
    fun sendBedrockParticle(livingEntity: LivingEntity, position: Vec3, particle: ResourceLocation) {
        val nearbyPlayers = livingEntity.level().players().filter { it.distanceTo(livingEntity) < 1000 }

        nearbyPlayers.forEach {
            (it as ServerPlayer).sendPacket(
                SpawnSnowstormParticlePacket(
                particle,
                position)
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

}