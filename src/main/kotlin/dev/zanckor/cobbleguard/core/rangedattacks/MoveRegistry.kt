package dev.zanckor.cobbleguard.core.rangedattacks

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import dev.zanckor.cobbleguard.core.rangedattacks.moves.FireMove
import dev.zanckor.cobbleguard.core.rangedattacks.moves.GravityMove
import dev.zanckor.cobbleguard.core.rangedattacks.moves.IceMove
import dev.zanckor.cobbleguard.core.rangedattacks.moves.PoisonMove

class MoveRegistry {
    private val moves: MutableMap<ElementalType, AttackMove> = mutableMapOf()

    init {
        registerMove(ElementalTypes.FIRE, FireMove(isRanged = true, isTickEffect = true, type = ElementalTypes.FIRE, speed = 0.75F))
        registerMove(ElementalTypes.ICE, IceMove(isRanged = true, isTickEffect = true, type = ElementalTypes.ICE, speed = 4F))
        registerMove(ElementalTypes.POISON, PoisonMove(isRanged = true, isTickEffect = true, type = ElementalTypes.POISON, speed = 0.5F))
        registerMove(ElementalTypes.PSYCHIC, GravityMove(isRanged = true, isTickEffect = true, type = ElementalTypes.PSYCHIC, speed = 0.5F))
    }

    private fun registerMove(elementalType: ElementalType, move: AttackMove) {
        moves[elementalType] = move
    }

    fun getMove(elementalType: ElementalType): AttackMove? {
        return moves[elementalType]
    }
}