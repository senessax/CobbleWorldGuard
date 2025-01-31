package dev.zanckor.cobbleguard.mixin.mixin

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

@Mixin(Projectile::class)
abstract class ProjectileMixin : RangedMove {

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

    @Inject(method = ["onHitEntity"], at = [At("HEAD")])
    private fun onHitEntity(entityHitResult: EntityHitResult, ci: CallbackInfo) {
        move?.let {
            val entity = entityHitResult.entity
            if (entity is LivingEntity) {
                it.applyEffect(entity)
            }
        }
    }

    @Inject(method = ["tick"], at = [At("HEAD")])
    private fun onTick(ci: CallbackInfo) {
        move?.let {
            (this as Projectile).apply {
                it.renderParticle(this)
                isNoGravity = true
                if (tickCount >= 200) {
                    remove(Entity.RemovalReason.DISCARDED)
                }
            }
        }
    }
}
