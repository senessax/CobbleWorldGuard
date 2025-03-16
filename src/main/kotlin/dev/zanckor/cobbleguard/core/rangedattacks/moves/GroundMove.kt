package dev.zanckor.cobbleguard.core.rangedattacks.moves

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.util.CobbleUtil
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile

class GroundMove(
    override var isRanged: Boolean,
    override val isTickEffect: Boolean,
    override val type: ElementalType?,
    override val speed: Float,
    override var damage: Double = 0.0,
) : AttackMove {

    override fun applyEffect(owner: PokemonEntity, target: LivingEntity) {
        target.hurt(target.damageSources().generic(), damage.toFloat())
        target.remainingFireTicks = (damage * 5).toInt()
    }

    override fun renderParticleOnAttack(projectile: Projectile) {
        if(projectile.owner == null) return

        CobbleUtil.summonEntityParticles(projectile.owner!!, CobbleUtil.SANDATTACK)
        CobbleUtil.summonEntityParticles(projectile.owner!!, CobbleUtil.SANDATTACK_RESIDUAL)
    }

    override fun renderParticleOnHit(target: LivingEntity) {
        CobbleUtil.summonHitParticles(target, CobbleUtil.SANDATTACK_IMPACT)
    }
}