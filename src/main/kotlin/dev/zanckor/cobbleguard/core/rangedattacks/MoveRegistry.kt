package dev.zanckor.cobbleguard.core.rangedattacks

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes

class MoveRegistry {
    private val moves: MutableMap<ElementalType, AttackMove> = mutableMapOf()

    init {
        registerMove(ElementalTypes.FIRE, FireMove(
            isRanged = true,
            isTickEffect = true,
            type = ElementalTypes.FIRE,
            speed = 0.75F
        ))
    }

    private fun registerMove(elementalType: ElementalType, move: AttackMove) {
        moves[elementalType] = move
    }

    fun getMove(elementalType: ElementalType): AttackMove? {
        return moves[elementalType]
    }
}