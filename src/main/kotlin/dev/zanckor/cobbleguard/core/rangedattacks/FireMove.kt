package dev.zanckor.cobbleguard.core.rangedattacks

import com.cobblemon.mod.common.api.types.ElementalType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import kotlin.math.cos
import kotlin.math.sin

class FireMove(
    override var isRanged: Boolean,
    override val isTickEffect: Boolean,
    override val type: ElementalType?,
    override val speed: Float,
    override var damage: Double = 0.0,
) : AttackMove {

    override fun applyEffect(target: LivingEntity) {
        target.hurt(target.damageSources().generic(), damage.toFloat())
        target.remainingFireTicks = (damage * 5).toInt()
    }

    override fun renderParticle(projectile: Projectile) {
        val level = projectile.level()
        if (level !is ServerLevel) return

        val numParticles = 5
        val radius = 0.15
        val heightStep = 0
        val turns = 1

        for (i in 0 until (numParticles * turns)) {
            val angle = (i.toDouble() / numParticles) * (2 * Math.PI)
            val xOffset = cos(angle) * radius
            val zOffset = sin(angle) * radius
            val yOffset = (i.toDouble() * heightStep) - ((numParticles * turns) * heightStep) / 2

            val x = projectile.x + xOffset
            val y = projectile.y + yOffset
            val z = projectile.z + zOffset

            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }
}