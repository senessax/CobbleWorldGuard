package dev.zanckor.cobbleguard.mixin.mixininterface

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove

interface RangedMove {
    fun getOwner(): PokemonEntity?
    fun setOwner(owner: PokemonEntity?)

    fun getMove(): AttackMove?
    fun setMove(move: AttackMove?)

    fun setMoveDamage(damage: Double)
}