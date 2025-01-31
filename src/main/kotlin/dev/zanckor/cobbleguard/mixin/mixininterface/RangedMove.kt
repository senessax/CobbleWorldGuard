package dev.zanckor.cobbleguard.mixin.mixininterface

import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove

interface RangedMove {
    fun getMove(): AttackMove?
    fun setMove(move: AttackMove?)

    fun setMoveDamage(damage: Double)
}