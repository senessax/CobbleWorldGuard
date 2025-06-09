package dev.zanckor.cobbleguard.core.command

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.CommandSelection
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData

class CommandRegistry {

    fun giveItemCommand() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<CommandSourceStack?>,
                                                                                 _: CommandBuildContext?,
                                                                                 _: CommandSelection? ->

            dispatcher.register(
                Commands.literal("cobbleguard")
                    .then(
                        Commands.literal("wand")
                            .executes { context ->
                                val player = context.source.player
                                val item = ItemStack(Items.STICK)
                                val nbt = CompoundTag()
                                nbt.putBoolean("isGuardItem", true)
                                item.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt))
                                player!!.inventory.add(item)
                                1
                            }
                    )
                    .then(
                        Commands.literal("reload")
                            .requires { it.hasPermission(2) }
                            .executes { context ->
                                dev.zanckor.cobbleguard.config.SimpleConfig.reload()
                                context.source.sendSuccess({ net.minecraft.network.chat.Component.literal("CobbleGuard config reloaded.") }, false)
                                1
                            }
                    )
                )
            }
        )
    }

    companion object {
        fun init() {
            CommandRegistry().giveItemCommand()
        }
    }
}
