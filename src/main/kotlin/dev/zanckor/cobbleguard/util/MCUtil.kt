package dev.zanckor.cobbleguard.util

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB

object MCUtil {

    fun getEntityLookingAt(player: Player, reachDistance : Double): LivingEntity? {
        val eyePosition = player.eyePosition
        val lookVector = player.lookAngle

        val targetPosition = eyePosition.add(lookVector.scale(reachDistance))
        val searchBox = AABB(
            eyePosition.x, eyePosition.y, eyePosition.z,
            targetPosition.x, targetPosition.y, targetPosition.z
        ).inflate(1.0)

        val possibleTargets = player.level().getEntities(
            player,
            searchBox
        ) { it is LivingEntity }

        var closestEntity: LivingEntity? = null
        var closestDistance = Double.MAX_VALUE

        for (entity in possibleTargets) {
            if (entity !is LivingEntity) continue

            val hitBox = entity.boundingBox
            val intersection = hitBox.clip(eyePosition, targetPosition)

            if (intersection.isPresent) {
                val distance = eyePosition.distanceToSqr(intersection.get())
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestEntity = entity
                }
            }
        }

        return closestEntity
    }
}