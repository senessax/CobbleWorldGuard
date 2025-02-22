package dev.zanckor.cobbleguard.core.rangedattacks.moves

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.zanckor.cobbleguard.core.rangedattacks.AttackMove
import dev.zanckor.cobbleguard.util.CobbleUtil
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.Level

class ThunderballMove(
    override var isRanged: Boolean,
    override val isTickEffect: Boolean,
    override val type: ElementalType?,
    override val speed: Float,
    override var damage: Double = 0.0,
) : AttackMove {

    override fun applyEffect(owner: PokemonEntity, target: LivingEntity) {
        target.hurt(target.damageSources().generic(), damage.toFloat())

        target.level().explode(null, target.x, target.y, target.z, 5.0F, false, Level.ExplosionInteraction.MOB)
    }

    override fun renderTickParticle(projectile: Projectile) {
        CobbleUtil.summonHitParticles(projectile.level(), projectile.position(), CobbleUtil.ELECTRICIMPACT)
    }
}