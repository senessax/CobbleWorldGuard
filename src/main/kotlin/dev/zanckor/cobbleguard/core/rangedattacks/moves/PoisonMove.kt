package dev.zanckor.cobbleguard.core.rangedattacks.moves

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.mixin.mixininterface.EffectContainer
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PoisonMove(
    override var isRanged: Boolean,
    override val isTickEffect: Boolean,
    override val type: ElementalType?,
    override val speed: Float,
    override var damage: Double = 0.0,
) : AttackMove {

    override fun applyEffect(target: LivingEntity) {
        target.hurt(target.damageSources().generic(), damage.toFloat())
        target.addEffect(MobEffectInstance(MobEffects.POISON, 30, 2))

        onHitParticle(target)
    }
    private fun onHitParticle(entity: LivingEntity) {
        val level = entity.level()
        if (level !is ServerLevel) return

        val numParticles = 20
        val maxRadius = 1.0 // Max radius of the sphere
        val heightStep = 0.25
        val particleHeight = 3.0

        for (i in 0 until numParticles) {
            val yOffset = (i.toDouble() * heightStep) - particleHeight / 2
            val radius = maxRadius * (yOffset + particleHeight / 2) / particleHeight // Increase radius with height

            val phi = acos(1 - 2 * (i.toDouble() / numParticles)) // Longitude angle (0 to PI)
            val theta = Math.PI * (1 + sqrt(5.0)) * i.toDouble() // Latitude angle (0 to 2PI)

            val xOffset = radius * sin(phi) * cos(theta)
            val yOffsetSphere = radius * cos(phi)
            val zOffset = radius * sin(phi) * sin(theta)

            val x = entity.x + xOffset
            val y = entity.y + yOffsetSphere + yOffset // Adding yOffset to control vertical position
            val z = entity.z + zOffset

            level.sendParticles(ParticleTypes.WITCH, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override fun renderParticle(projectile: Projectile) {
        val level = projectile.level()
        if (level !is ServerLevel) return

        val direction = projectile.deltaMovement.normalize()
        val offset = direction.scale(-0.5)
        val offset2 = direction.scale(-0.25)

        val positions = listOf(
            Triple(projectile.x + offset.x, projectile.y, projectile.z + offset.z),
            Triple(projectile.x - offset.x, projectile.y, projectile.z - offset.z),
            Triple(projectile.x + offset2.x, projectile.y, projectile.z + offset2.z),
            Triple(projectile.x - offset2.x, projectile.y, projectile.z - offset2.z),
        )

        positions.forEach { (x, y, z) ->
            level.sendParticles(ParticleTypes.TRIAL_OMEN, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }
}