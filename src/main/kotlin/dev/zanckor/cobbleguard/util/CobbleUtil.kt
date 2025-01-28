package dev.zanckor.cobbleguard.util

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.LivingEntity

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
}