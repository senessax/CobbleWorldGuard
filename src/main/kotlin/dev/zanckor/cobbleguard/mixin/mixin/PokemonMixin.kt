package dev.zanckor.cobbleguard.mixin.mixin

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.mixin.mixininterface.Hostilemon
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.level.Level
import net.tslat.smartbrainlib.api.SmartBrainOwner
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import org.spongepowered.asm.mixin.Mixin

@Mixin(PokemonEntity::class)
class PokemonMixin(entityType: EntityType<out PathfinderMob>, level: Level,
                   override var isHostile: Boolean,
                   override val randomMove: Move?
) :
    PathfinderMob(entityType, level),
    Hostilemon,
    SmartBrainOwner<PokemonMixin> {

    override fun useMove(move: Move?, target: LivingEntity?) {

    }

    override fun attack(target: LivingEntity?, damage: Double) {

    }

    override fun getSensors(): MutableList<out ExtendedSensor<out PokemonMixin>> {
        val sensors = mutableListOf<ExtendedSensor<out PokemonMixin>>()

        return sensors
    }
}