package dev.zanckor.cobbleguard.wrapper.claim;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.protection.api.IChunkProtectionAPI;

public class OpacClaim implements IClaimWrapper {

    @Override
    public boolean canWorkOnClaim(PokemonEntity entity, LivingEntity target, ServerLevel level) {
        OpenPACServerAPI opacServerAPI = OpenPACServerAPI.get(level.getServer());
        ResourceLocation dimension = level.dimension().location();
        IPlayerChunkClaimAPI claimState = opacServerAPI.getServerClaimsManager().get(dimension, target.getOnPos());

        IChunkProtectionAPI chunkProtectionAPI = opacServerAPI.getChunkProtection();
        boolean isBlockProtected = chunkProtectionAPI.onEntityInteraction(
                entity, entity, target,
                Items.AIR.getDefaultInstance(), InteractionHand.MAIN_HAND,
                true, false, false);

        return claimState == null || !isBlockProtected;
    }
}
