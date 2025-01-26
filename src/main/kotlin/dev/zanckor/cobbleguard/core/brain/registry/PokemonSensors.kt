package dev.zanckor.cobbleguard.core.brain.registry

import dev.zanckor.cobbleguard.CobbleGuard
import dev.zanckor.cobbleguard.core.brain.sensor.NearestHostileMobSensor
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.sensing.SensorType
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import java.util.function.Supplier

object PokemonSensors {
    val NEAREST_TARGET = register("nearest_target_sensor") { NearestHostileMobSensor() }

    private fun register(key: String, factory: Supplier<ExtendedSensor<*>>): SensorType<out ExtendedSensor<*>> {
        return Registry.register(BuiltInRegistries.SENSOR_TYPE, ResourceLocation.fromNamespaceAndPath(CobbleGuard.MODID, key), SensorType(factory))
    }

    fun init() {
    }
}