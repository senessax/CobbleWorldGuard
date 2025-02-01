package dev.zanckor.cobbleguard.core.rangedattacks

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile

interface AttackMove {
    var isRanged : Boolean // This is used to check if the snowball has been initialized as a ranged move
    val isTickEffect : Boolean
    val type : ElementalType?
    val speed : Float
    var damage : Double

    fun applyEffect(owner: PokemonEntity, target: LivingEntity)

    fun renderParticle(projectile: Projectile) {
    }
}