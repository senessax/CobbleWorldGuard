package dev.zanckor.cobbleguard.wrapper.claim;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface IClaimWrapper {

    /**
     * Check if the entity can work on the claim at the given position.
     *
     * @param entity   The entity that wants to work on the claim.
     * @param target  The target entity that the entity wants to work on.
     * @return True if the entity can work on the claim, false otherwise.
     */
    boolean canWorkOnClaim(PokemonEntity entity, LivingEntity target, ServerLevel level);
}
