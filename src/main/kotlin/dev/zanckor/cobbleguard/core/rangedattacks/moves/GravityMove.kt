package dev.zanckor.cobbleguard.core.rangedattacks.moves

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.mixin.mixin.PokemonMixin
import dev.zanckor.cobbleguard.mixin.mixininterface.EffectContainer
import dev.zanckor.cobbleguard.util.CobbleUtil
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

class GravityMove(
    override var isRanged: Boolean,
    override val isTickEffect: Boolean,
    override val type: ElementalType?,
    override val speed: Float,
    override var damage: Double = 0.0,
) : AttackMove {

    override fun applyEffect(owner: PokemonEntity, target: LivingEntity) {
        target.hurt(target.damageSources().generic(), damage.toFloat())
        (target as EffectContainer).addEffect(ElementalTypes.PSYCHIC, 200)
    }

    override fun renderParticle(projectile: Projectile) {
        CobbleUtil.summonRangedParticles(projectile.owner as PokemonEntity, CobbleUtil.HYPNOSIS)
    }
}