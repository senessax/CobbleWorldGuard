package dev.zanckor.cobbleguard.core.rangedattacks

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import dev.zanckor.cobbleguard.core.rangedattacks.moves.*

object MoveRegistry {
    private val moves: MutableMap<ElementalType, AttackMove> = mutableMapOf()
    val FireMove: AttackMove
    val IceMove: AttackMove
    val WaterMove: AttackMove
    val PoisonMove: AttackMove
    val ElectricMove: AttackMove
    val ThunderballMove: AttackMove
    val GrassMove: AttackMove
    val BugMove: AttackMove
    val GroundMove: AttackMove
    val RockMove: AttackMove

    init {
        FireMove = registerMove(ElementalTypes.FIRE, FireMove(isRanged = true, isTickEffect = true, type = ElementalTypes.FIRE, speed = 0.75F))
        IceMove = registerMove(ElementalTypes.ICE, WaterMove(isRanged = true, isTickEffect = true, type = ElementalTypes.ICE, speed = 4F))
        WaterMove = registerMove(ElementalTypes.WATER, WaterMove(isRanged = true, isTickEffect = true, type = ElementalTypes.WATER, speed = 2F))
        PoisonMove = registerMove(ElementalTypes.POISON, PoisonMove(isRanged = true, isTickEffect = true, type = ElementalTypes.POISON, speed = 0.5F))
        ElectricMove = registerMove(ElementalTypes.ELECTRIC, ElectricMove(isRanged = true, isTickEffect = true, type = ElementalTypes.ELECTRIC, speed = 5F))
        ThunderballMove = registerMove(ElementalTypes.ELECTRIC, ThunderballMove(isRanged = true, isTickEffect = true, type = ElementalTypes.ELECTRIC, speed = 3F))
        GrassMove = registerMove(ElementalTypes.GRASS, BugMove(isRanged = true, isTickEffect = true, type = ElementalTypes.GRASS, speed = 2F))
        BugMove = registerMove(ElementalTypes.BUG, BugMove(isRanged = true, isTickEffect = true, type = ElementalTypes.BUG, speed = 3F))
        GroundMove = registerMove(ElementalTypes.GROUND, GroundMove(isRanged = true, isTickEffect = true, type = ElementalTypes.GROUND, speed = 5F))
        RockMove = registerMove(ElementalTypes.ROCK, RockMove(isRanged = true, isTickEffect = true, type = ElementalTypes.ROCK, speed = 3F))
    }

    private fun registerMove(elementalType: ElementalType, move: AttackMove): AttackMove {
        moves[elementalType] = move

        return move
    }

    fun getMove(elementalType: ElementalType): AttackMove? {
        return moves[elementalType]
    }
}