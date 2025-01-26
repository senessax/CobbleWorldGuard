package dev.zanckor.cobbleguard.mixin.mixininterface

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.types.ElementalType
import net.minecraft.world.entity.LivingEntity

interface Hostilemon {
    var isHostile: Boolean

    fun getBestMoveAgainst(target: LivingEntity?): Move?

    fun useMove(move: Move?, target: LivingEntity?)

    fun getMoveEffectiveness(move: Move?, targetType: ElementalType): Double
}
