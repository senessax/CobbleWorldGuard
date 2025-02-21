package dev.zanckor.cobbleguard.core.rangedattacks.moves

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.mixin.mixininterface.EffectContainer
import dev.zanckor.cobbleguard.util.CobbleUtil
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import kotlin.math.cos
import kotlin.math.sin

class IceMove(
    override var isRanged: Boolean,
    override val isTickEffect: Boolean,
    override val type: ElementalType?,
    override val speed: Float,
    override var damage: Double = 0.0,
) : AttackMove {

    override fun applyEffect(owner: PokemonEntity, target: LivingEntity) {
        target.hurt(target.damageSources().generic(), damage.toFloat())
        (target as EffectContainer).addEffect(ElementalTypes.ICE, 200)

        onHitParticle(target)
    }

    private fun onHitParticle(entity: LivingEntity) {
        val level = entity.level()
        if (level !is ServerLevel) return

        val numParticles = 20
        val radius = 0.5
        val heightStep = 0.25
        val particleHeight = 3.0

        // Cuatro direcciones en las que se distribuirán los picos
        val angles = arrayOf(0.0, Math.PI / 2, Math.PI, 3 * Math.PI / 2)

        for (angleOffset in angles) {
            for (i in 0 until numParticles) {
                val yOffset = (i.toDouble() * heightStep) - particleHeight / 2

                val angle = (i.toDouble() / numParticles) * (2 * Math.PI) + angleOffset
                val xOffset = cos(angle) * radius
                val zOffset = sin(angle) * radius

                val x = entity.x + xOffset
                val y = entity.y + yOffset
                val z = entity.z + zOffset

                level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
            }
        }
    }



    override fun renderParticle(projectile: Projectile) {
        CobbleUtil.summonRangedParticles(projectile.owner as PokemonEntity, CobbleUtil.BUBBLEBEAM)
    }
}