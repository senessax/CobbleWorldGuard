package dev.zanckor.cobbleguard.mixin.mixin

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.goals.PokemonFollowOwnerGoal
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(PokemonFollowOwnerGoal::class)
abstract class PokemonFollowOwnerGoalMixin {
    @Shadow
    private val entity: PokemonEntity? = null

    @Inject(method = ["canContinueToUse"], at = [At("RETURN")], cancellable = true)
    private fun canContinueToUse(cir: CallbackInfoReturnable<Boolean>) {
        if (entity != null) {
            val hasTarget = entity.target != null && entity.target!!.isAlive
            val isStaying = (entity as Hostilemon).aggressivity == Hostilemon.Aggresivity.STAY

            if(hasTarget) cir.returnValue = false
            if(isStaying) cir.returnValue = false
        }
    }

    @Inject(method = ["canUse"], at = [At("RETURN")], cancellable = true)
    private fun canUse(cir: CallbackInfoReturnable<Boolean>) {
        if (entity != null) {
            val hasTarget = entity.target != null && entity.target!!.isAlive
            val isStaying = (entity as Hostilemon).aggressivity == Hostilemon.Aggresivity.STAY

            if(hasTarget) cir.returnValue = false
            if(isStaying) cir.returnValue = false
        }
    }
}