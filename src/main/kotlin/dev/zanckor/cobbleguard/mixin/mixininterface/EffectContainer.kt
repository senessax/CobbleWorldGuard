package dev.zanckor.cobbleguard.mixin.mixininterface

import com.cobblemon.mod.common.api.types.ElementalType

interface EffectContainer {

    /**
     * This is a list of pairs that contain the elemental type and the duration of the effect
     * Duration is in ticks
     */
    fun addEffect(elementalType: ElementalType, tickDuration: Int)

    /**
     * Returns the first effect in the list
     */
    fun getEffectType(): Pair<ElementalType, Int>

    /**
     * Updates the duration of the effect
     */
    fun updateDuration()
}