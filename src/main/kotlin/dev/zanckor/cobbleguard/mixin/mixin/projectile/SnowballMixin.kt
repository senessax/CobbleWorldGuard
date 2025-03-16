package dev.zanckor.cobbleguard.mixin.mixin.projectile

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.mixin.mixininterface.RangedMove
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Fireball
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.Snowball
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(Snowball::class)
@Debug(export = true, print = true)
abstract class SnowballMixin(
    entityType: EntityType<out ThrowableItemProjectile>,
    level: Level
) : RangedMove, ThrowableItemProjectile(entityType, level) {

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

    public override fun onHit(hitResult: HitResult) {
        if (hitResult is EntityHitResult) {
            move?.let {
                val entity = hitResult.entity

                if (entity is LivingEntity && owner != null) {
                    it.applyEffect(owner!!, entity)
                    it.renderParticleOnHit(entity)
                }
            }
        }

        super.onHit(hitResult)
    }

    override fun tick() {
        super.tick()

        move?.let {
            val projectile = this as Projectile
            projectile.isNoGravity = true
            if (projectile.tickCount >= 200) {
                projectile.remove(RemovalReason.DISCARDED)
            }
            move?.renderTickParticle(projectile)
        }
    }

    override fun shoot(x: Double, y: Double, z: Double, g: Float, h: Float) {
        move?.renderParticleOnAttack(this as Projectile)

        super.shoot(x, y, z, g, h)
    }
}