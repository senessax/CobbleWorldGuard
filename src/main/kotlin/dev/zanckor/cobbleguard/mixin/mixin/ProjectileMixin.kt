package dev.zanckor.cobbleguard.mixin.mixin

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.mixin.mixininterface.RangedMove
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.EntityHitResult
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Suppress("CAST_NEVER_SUCCEEDS")
@Mixin(Projectile::class)
abstract class ProjectileMixin : RangedMove {

    @Unique
    private var owner: PokemonEntity? = null

    @Unique
    private var move: AttackMove? = null

    override fun setMove(move: AttackMove?) {
        this.move = move
    }

    override fun getMove(): AttackMove? {
        return this.move
    }

    override fun setMoveDamage(damage: Double) {
        move?.damage = damage
    }

    override fun getOwner(): PokemonEntity? {
        return owner
    }

    override fun setOwner(owner: PokemonEntity?) {
        this.owner = owner
    }

    @Inject(method = ["onHitEntity"], at = [At("HEAD")])
    private fun onHitEntity(entityHitResult: EntityHitResult, ci: CallbackInfo) {
        move?.let {
            val entity = entityHitResult.entity

            if (entity is LivingEntity && owner != null) {
                it.applyEffect(owner!!, entity)
            }
        }
    }

    @Inject(method = ["tick"], at = [At("HEAD")])
    private fun onTick(ci: CallbackInfo) {
        move?.let {
            with(this as Projectile) {
                isNoGravity = true
                if (tickCount >= 200) {
                    remove(Entity.RemovalReason.DISCARDED)
                }
            }
        }
    }

    @Inject(method = ["shoot"], at = [At("HEAD")])
    private fun onShoot(ci: CallbackInfo) {
        move?.renderParticle(this as Projectile)
    }
}
