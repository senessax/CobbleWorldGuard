package dev.zanckor.cobbleguard.mixin.mixininterface

import com.cobblemon.mod.common.api.moves.Move
import net.minecraft.world.entity.LivingEntity

interface Hostilemon {
    var isHostile: Boolean

    val randomMove: Move?

    fun useMove(move: Move?, target: LivingEntity?)
    fun attack(target: LivingEntity?, damage: Double)
}
