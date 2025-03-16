package dev.zanckor.cobbleguard.wrapper.claim;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class FlanClaim implements IClaimWrapper {

    @Override
    public boolean canWorkOnClaim(PokemonEntity entity, LivingEntity target, ServerLevel level) {
        Claim claim = ClaimStorage.get(level).getClaimAt(target.getOnPos());

        return claim == null || claim.canAttackEntity(target);
    }
}
