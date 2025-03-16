package dev.zanckor.cobbleguard.wrapper;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.zanckor.cobbleguard.wrapper.claim.FlanClaim;
import dev.zanckor.cobbleguard.wrapper.claim.GriefDefenderClaim;
import dev.zanckor.cobbleguard.wrapper.claim.OpacClaim;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ClaimApiWrapper {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canAttack(PokemonEntity entity, LivingEntity target) {
        if(entity.getPokemon().getOwnerPlayer() == null) return true;

        Level level = entity.level();

        boolean hasFlan = FabricLoader.getInstance().isModLoaded("flan");
        boolean hasOpenParties = FabricLoader.getInstance().isModLoaded("openpartiesandclaims");
        boolean hasGD = FabricLoader.getInstance().isModLoaded("griefdefender");

        if (hasGD) return new GriefDefenderClaim().canWorkOnClaim(entity, target, (ServerLevel) level);
        if (hasOpenParties) return new OpacClaim().canWorkOnClaim(entity, target, (ServerLevel) level);
        if (hasFlan) return new FlanClaim().canWorkOnClaim(entity, target, (ServerLevel) level);

        return true;
    }
}