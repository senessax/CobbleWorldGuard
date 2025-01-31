package dev.zanckor.cobbleguard.mixin.mixininterface

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.types.ElementalType
import net.minecraft.world.entity.LivingEntity

interface Hostilemon {
    var isHostile: Boolean
    var aggressivity: Aggresivity

    fun getBestMoveAgainst(target: LivingEntity?): Move?

    fun usePhysicalMove(move: Move?, target: LivingEntity?)

    fun useRangedMove(move: Move?, target: LivingEntity?)

    fun getMoveEffectiveness(move: Move?, targetType: ElementalType): Double

    enum class Aggresivity(val message: String) {
        STAY("Pokemon will stay in place"),
        PASSIVE("Pokemon will never attack"),
        DEFENSIVE("Pokemon will protect its owner and retaliate if attacked"),
        HOSTILE("Pokemon will attack enemy entities. (Monsters)"),
        AGGRESIVE("Pokemon will attack all entities except owner's allies");

        fun next(): Aggresivity {
            return if(this.ordinal == entries.size - 1) {
                entries[0]
            } else {
                entries[this.ordinal + 1]
            }
        }
    }
}
