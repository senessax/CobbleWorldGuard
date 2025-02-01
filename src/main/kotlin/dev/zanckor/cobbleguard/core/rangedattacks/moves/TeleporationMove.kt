package dev.zanckor.cobbleguard.core.rangedattacks.moves

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.mixin.mixin.PokemonMixin
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

class TeleporationMove(
    override var isRanged: Boolean = false,
    override val isTickEffect: Boolean = false,
    override val type: ElementalType? = ElementalTypes.PSYCHIC,
    override val speed: Float = 0.0F,
    override var damage: Double = 0.0,
) : AttackMove {

    override fun applyEffect(owner: PokemonEntity, target: LivingEntity) {
        val random = Random()

        val offsetX = random.nextDouble() * 20 - 10
        val offsetY = random.nextDouble() * 2 - 1
        val offsetZ = random.nextDouble() * 20 - 10

        val newX = owner.x + offsetX
        val newY = owner.y + offsetY
        val newZ = owner.z + offsetZ

        customParticle(owner)
        owner.setPos(newX, newY, newZ)
    }

    private fun customParticle(owner: PokemonEntity) {
        val level = owner.level()
        if (level !is ServerLevel) return

        val numParticles = 30
        val radius = 0.1  // The width of the teleportation effect
        val heightStep = 0.2  // Vertical step for particle placement
        val particleSpeed = 0.05  // Controls how quickly particles move up
        val startY = owner.y

        // For each particle, adjust the position and simulate upward motion
        for (i in 0 until numParticles) {
            // Calculate the vertical position and horizontal spread (small width)
            val yOffset = i.toDouble() * heightStep
            val angle = Math.random() * 2 * Math.PI  // Random horizontal spread for a natural effect
            val xOffset = cos(angle) * radius
            val zOffset = sin(angle) * radius

            // Calculate the final position of the particle
            val x = owner.x + xOffset
            val y = startY + yOffset + sin(System.currentTimeMillis() / 1000.0) * 0.05  // Slight sine movement for a wave effect
            val z = owner.z + zOffset

            // Emit the particle at the calculated position
            level.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0.0, 0.0, 0.0, particleSpeed)
        }
    }

    override fun renderParticle(projectile: Projectile) {
    }
}