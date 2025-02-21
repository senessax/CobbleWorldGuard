package dev.zanckor.cobbleguard.util

import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB

object MCUtil {

    fun getEntityLookingAt(rayTraceEntity: Entity, distance: Double): LivingEntity? {
        val playerRotX = rayTraceEntity.xRot
        val playerRotY = rayTraceEntity.yRot
        var startPos = rayTraceEntity.eyePosition

        val f2 = Mth.cos(-playerRotY * (Math.PI.toFloat() / 180F) - Math.PI.toFloat())
        val f3 = Mth.sin(-playerRotY * (Math.PI.toFloat() / 180F) - Math.PI.toFloat())
        val f4 = -Mth.cos(-playerRotX * (Math.PI.toFloat() / 180F))
        val additionY = Mth.sin(-playerRotX * (Math.PI.toFloat() / 180F))
        val additionX = f3 * f4
        val additionZ = f2 * f4

        var d0 = distance
        val endVec = startPos.add(additionX * d0, additionY * d0, additionZ * d0)

        val startEndBox = AABB(startPos, endVec)
        var entity: Entity? = null

        for (entity1 in rayTraceEntity.level().getEntities(rayTraceEntity, startEndBox) { true }) {
            val aabb = entity1.boundingBox.inflate(entity1.pickRadius.toDouble())
            val optional = aabb.clip(startPos, endVec)

            if (aabb.contains(startPos)) {
                if (d0 >= 0.0) {
                    entity = entity1
                    startPos = optional.orElse(startPos)
                    d0 = 0.0
                }
            } else if (optional.isPresent) {
                val vec31 = optional.get()
                val d1 = startPos.distanceToSqr(vec31)

                if (d1 < d0 || d0 == 0.0) {
                    if (entity1.rootVehicle == rayTraceEntity.rootVehicle) {
                        if (d0 == 0.0) {
                            entity = entity1
                            startPos = vec31
                        }
                    } else {
                        entity = entity1
                        startPos = vec31
                        d0 = d1
                    }
                }
            }
        }

        return if(entity is LivingEntity) entity else null
    }
}