package dev.zanckor.cobbleguard.mixin.mixin

import com.cobblemon.mod.common.entity.pokemon.ai.goals.PokemonFollowOwnerGoal
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(PokemonFollowOwnerGoal::class)
abstract class PokemonFollowOwnerGoalMixin {

    @Inject(method = ["canContinueToUse"], at = [At("HEAD")], cancellable = true)
    private fun canContinueToUse(cir: CallbackInfoReturnable<Boolean>) {
        cir.returnValue = false
    }

    @Inject(method = ["canUse"], at = [At("HEAD")], cancellable = true)
    private fun canUse(cir: CallbackInfoReturnable<Boolean>) {
        println("canUse")
        cir.returnValue = false
    }
}
