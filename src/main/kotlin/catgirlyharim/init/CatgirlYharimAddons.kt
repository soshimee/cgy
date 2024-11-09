package catgirlyharim.init

import catgirlyharim.init.config.MyConfig
import catgirlyharim.init.features.AlwaysSprint
import catgirlyharim.init.features.AutoP3
import catgirlyharim.init.features.AutoP3.P3Command
import catgirlyharim.init.features.RingManager
import catgirlyharim.init.features.Simulation
import catgirlyharim.init.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.lwjgl.input.Keyboard


class CgyCommands : CommandBase() {
    override fun getCommandName(): String {
        return "CatgirlYharimAddons"
    }

    override fun getCommandAliases(): List<String> {
        return listOf(
            "cgy"
        )
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        CatgirlYharim.config?.openGui()
    }
}

@Mod(
    modid = "cgy",
    name = "CatgirlYharimAddons",
    version = "1.0.0",
    clientSideOnly = true
)

class CatgirlYharim {
    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        config = MyConfig()
        listOf(
            CgyCommands(),
        ).forEach {
            ClientCommandHandler.instance.registerCommand((it))
        }

        listOf(
            this,
            P3Command,
            Hclip,
            MyConfig(),
            AutoP3,
            ServerRotateUtils,
            lavaClip,
            edgeJump,
            Simulation,
            AlwaysSprint,
            HUDRenderUtils,
            MovementUtils,
            WorldRenderUtils,
            ClientListener,
            Utils,
            RingManager
        ).forEach(MinecraftForge.EVENT_BUS::register)

        //keyBinds.forEach(ClientRegistry::registerKeyBinding)
    }
/*
    @Subscribe
    fun onInit(event: InitializationEvent?) {
        config = MyConfig()
    }*/

    companion object {
        val mc: Minecraft = Minecraft.getMinecraft()
        var config: MyConfig? = null
    }
}