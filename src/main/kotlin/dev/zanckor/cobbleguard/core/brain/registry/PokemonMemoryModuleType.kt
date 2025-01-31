package dev.zanckor.cobbleguard.core.brain.registry

import dev.zanckor.cobbleguard.CobbleGuard.Companion.MODID
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import java.util.*

object PokemonMemoryModuleType {
    val NEAREST_OWNER_TARGET: MemoryModuleType<LivingEntity> = register("nearest_owner_target")
    val NEAREST_WILD_POKEMON_TARGET: MemoryModuleType<LivingEntity> = register("nearest_wild_pokemon_target")

    private fun <U> register(key: String): MemoryModuleType<U> {
        @Suppress("UNCHECKED_CAST")
        return Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE,
            ResourceLocation.fromNamespaceAndPath(MODID, key),
            MemoryModuleType<Any?>(Optional.empty())
        ) as MemoryModuleType<U>
    }

    fun init() {
    }
}