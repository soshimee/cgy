package catgirlyharim.init.features

import catgirlyharim.init.CatgirlYharim.Companion.config
import catgirlyharim.init.CatgirlYharim.Companion.mc
import catgirlyharim.init.features.RingManager.allrings
import catgirlyharim.init.features.RingManager.loadRings
import catgirlyharim.init.features.RingManager.rings
import catgirlyharim.init.features.RingManager.saveRings
import catgirlyharim.init.utils.ClientListener.scheduleTask
import catgirlyharim.init.utils.Hclip.hclip
import catgirlyharim.init.utils.MovementUtils.jump
import catgirlyharim.init.utils.MovementUtils.stopMovement
import catgirlyharim.init.utils.MovementUtils.stopVelo
import catgirlyharim.init.utils.MovementUtils.walk
import catgirlyharim.init.utils.ServerRotateUtils.resetRotations
import catgirlyharim.init.utils.ServerRotateUtils.set
import catgirlyharim.init.utils.Utils.airClick
import catgirlyharim.init.utils.Utils.distanceToPlayer
import catgirlyharim.init.utils.Utils.getYawAndPitch
import catgirlyharim.init.utils.Utils.leftClick
import catgirlyharim.init.utils.Utils.modMessage
import catgirlyharim.init.utils.Utils.rotate
import catgirlyharim.init.utils.Utils.sendChat
import catgirlyharim.init.utils.Utils.swapFromName
import catgirlyharim.init.utils.WorldRenderUtils.drawSquareTwo
import catgirlyharim.init.utils.edgeJump.toggleEdging
import catgirlyharim.init.utils.lavaClip.toggleLavaClip
import cc.polyfrost.oneconfig.events.event.WorldLoadEvent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color.*
import java.io.File

import java.lang.Float.parseFloat
import kotlin.math.abs

object AutoP3 {
    var inp3 = false
    var cooldown = false

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        if (message.contains("[BOSS] Storm: I should have known that I stood no chance.")) {
            inp3 = true
            config!!.autoP3Active = true
            loadRings()
            //modMessage("P3 started!")
        }
        if (message.contains("[BOSS] Goldor: You have done it, you destroyed the factory…")) { // Change to necron death
            inp3 = false
            //modMessage("P3 ended!")
        }
    }

    @SubscribeEvent
    fun onRenderRing(event: RenderWorldLastEvent) {
        if (!config!!.autoP3Active || !inp3) return
        rings.forEach{ring ->
            if (ring.route != config!!.selectedRoute) return@forEach
            val color = when (ring.type) {
                "look" -> pink
                "stop" -> red
                "boom" -> cyan
                "jump" -> gray
                "hclip" -> black
                "bonzo" -> white
                "vclip" -> yellow
                "block" -> blue
                "edge" -> pink
                "walk" -> green
                else -> black
            }
            if(!ring.active) return
                drawSquareTwo(ring.x, ring.y + 0.05, ring.z, ring.width.toDouble(), ring.width.toDouble(), color, 4f, phase = false, relocate = true)
                drawSquareTwo(ring.x, ring.y + ring.height / 2, ring.z, ring.width.toDouble(), ring.width.toDouble(), color, 4f, phase = false, relocate = true)
                drawSquareTwo(ring.x, ring.y + ring.height, ring.z, ring.width.toDouble(), ring.width.toDouble(), color, 4f, phase = false, relocate = true)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!config!!.autoP3Active || !inp3) return
        val playerX = mc.renderManager.viewerPosX
        val playerY = mc.renderManager.viewerPosY
        val playerZ = mc.renderManager.viewerPosZ
        rings.forEach { ring ->
            if (ring.route != config!!.selectedRoute) return@forEach
            val distanceX = abs(playerX - ring.x)
            val distanceY = (playerY - ring.y)
            val distanceZ = abs(playerZ - ring.z)

            if ((distanceX > (ring.width / 2) || (distanceY >= (ring.height) || distanceY < 0) || distanceZ > (ring.width / 2))) {
                ring.active = true
            }

            if (!ring.active || config!!.editmode || cooldown) return@forEach
            if (distanceX < (ring.width / 2) && distanceY < (ring.height) && distanceY >= 0 && distanceZ < (ring.width / 2)) {
                ring.active = false
                if (ring.looking == true) rotate(ring.yaw, ring.pitch)
                if (ring.stopping == true) stopVelo()
                if (ring.walking == true) walk()
                when (ring.type) {
                    "walk" -> {
                        walk()
                        modMessage("Walking")
                    }
                    "look" -> {
                        modMessage("Looking")
                        rotate(ring.yaw, ring.pitch)
                    }
                    "stop" -> {
                        stopMovement()
                        stopVelo()
                        modMessage("Stopping")
                    }
                    "boom" ->  {
                        rotate(ring.yaw, ring.pitch)
                        swapFromName("boom")
                        if(ring.delaying == true) {
                            scheduleTask(ring.delay!!) { leftClick()}
                        } else {
                            scheduleTask(4) { leftClick()}
                        }
                        modMessage("Exploding")
                    }
                    "jump" -> {
                        jump()
                        modMessage("Jumping")
                    }
                    "hclip" -> {
                        hclip(ring.yaw)
                        if (ring.walking == true) {
                            scheduleTask(1) {
                                walk()
                            }
                        }
                        modMessage("Hclipping")
                    }
                    "bonzo" -> {
                        swapFromName("bonzo")
                        if (ring.silent == true) {
                            set(ring.yaw, ring.pitch)
                        } else {
                            rotate(ring.yaw, ring.pitch)
                        }
                        if (ring.delaying == true) {
                            scheduleTask(ring.delay!!) {
                                airClick()
                                resetRotations()
                            }
                        } else {
                            scheduleTask(1) {
                                airClick()
                                resetRotations()
                            }
                        }
                        modMessage("Bonzoing")
                    }
                    "vclip" -> {
                        ring.depth?.let { toggleLavaClip(it) }
                        modMessage("Clipping ${ring.depth} blocks down")
                    }
                    "block" -> {
                        val (yaw, pitch) = getYawAndPitch(ring.lookX!!, ring.lookY!!, ring.lookZ!!)
                        rotate(yaw, pitch)
                        modMessage("Rotating")
                    }
                    "edge" -> {
                        toggleEdging()
                        modMessage("Edging")
                    }
                    else -> sendChat("Invalid ring: ${ring.type}")
            }
            }
        }
    }


object P3Command : CommandBase() {
    override fun getCommandName(): String {
        return "p3"
    }

    override fun getCommandAliases(): List<String> {
        return listOf()
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender?, args: Array<String>) {
        if (args.isEmpty()) {
            modMessage("No argument specified!")
            return
        }
        when (args[0]) {
            "add" -> {
                val type = args[1]
                if (!arrayListOf(
                        "walk",
                        "look",
                        "stop",
                        "bonzo",
                        "boom",
                        "hclip",
                        "block",
                        "edge",
                        "vclip",
                        "jump"
                    ).contains((type))
                ) {
                    modMessage("Invalid ring!")
                    return
                }
                val toPush = Ring(type, active = true, route = config!!.selectedRoute, x = Math.round(mc.renderManager.viewerPosX * 2) / 2.0, y = Math.round(mc.renderManager.viewerPosY * 2) / 2.0, z = Math.round(mc.renderManager.viewerPosZ * 2) / 2.0, yaw = mc.renderManager.playerViewY, pitch = mc.renderManager.playerViewX, height = 1f, width = 1f, lookX = 1000.0, lookY = 1000.0, lookZ = 1000.0, depth = 1000f, stopping = false, looking = false, walking = false, silent = false, delaying = false, delay = 1000)
                if (args.size > 3 && type == "block") {
                    toPush.lookX = parseDouble(args[2])
                    toPush.lookY = parseDouble(args[3])
                    toPush.lookZ = parseDouble(args[4])
                }
                if (type == "vclip" && args.size > 2) {
                    val depth = parseFloat(args[2])
                    toPush.depth = depth
                }
                args.drop(2).forEachIndexed { _, arg ->
                    when {
                        arg.startsWith("h") -> toPush.height = arg.slice(1 until arg.length).toFloat()
                        arg.startsWith("w") && arg != "walk" -> toPush.width = arg.slice(1 until arg.length).toFloat()
                        arg == "stop" -> toPush.stopping = true
                        arg == "look" -> toPush.looking = true
                        arg == "walk" -> toPush.walking = true
                        arg == "silent" -> toPush.silent = true
                        arg.startsWith("delay:") -> {
                            val delay = arg.slice(6 until arg.length).toInt()
                            toPush.delaying = true
                            toPush.delay = delay
                        }
                    }
                }
                allrings.add(toPush)
                cooldown = true
                modMessage("$type added!")
                saveRings()
                loadRings()
                scheduleTask(19) {
                    cooldown = false
                }
            }
            "edit" -> {
                if (config!!.editmode) {
                    config!!.editmode = false
                    modMessage("Editmode off!")
                } else {
                    config!!.editmode = true
                    modMessage("Editmode on!")
                }
            }
            "start" -> {
                inp3 = true
                config!!.autoP3Active = true
                modMessage("P3 started!")
            }
            "stop" -> {
                inp3 = false
                modMessage("P3 stopped!")
            }
            "remove" -> {
                val range = args.getOrNull(1)?.toDoubleOrNull() ?: 2.0 // Default range to 2 if not provided
                allrings = allrings.filter { ring ->
                    // Filter rings based on the route and distance criteria
                    if (ring.route != config!!.selectedRoute) return@filter true
                    val distance = distanceToPlayer(ring.x, ring.y, ring.z)
                    distance >= range
                }.toMutableList()
                saveRings()
                loadRings()
            }
            "undo" -> {
                allrings.removeLast()
                saveRings()
                loadRings()
            }
            "clear" -> {
                val prefix = "§0[§6Yharim§0] §8»§r "
                sender?.addChatMessage(ChatComponentText("$prefix Are you sure?")
                    .apply {
                        chatStyle = ChatStyle().apply {
                            chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p3 clearconfirm")
                            chatHoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText("$prefix Click to clear ALL routes!")
                            )
                        }
                    }
                )
            }
            "clearroute" -> {
                val prefix = "§0[§6Yharim§0] §8»§r "
                sender?.addChatMessage(ChatComponentText("$prefix Are you sure?")
                    .apply {
                        chatStyle = ChatStyle().apply {
                            chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p3 clearrouteconfirm")
                            chatHoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText("$prefix Click to clear CURRENT route!")
                            )
                        }
                    }
                )
            }
            "clearrouteconfirm" -> {
                allrings = allrings.filter { ring ->
                    // Filter rings based on the route and distance criteria
                    ring.route != config!!.selectedRoute
                }.toMutableList()
                saveRings()
                loadRings()
            }
            "clearconfirm" -> {
                allrings = mutableListOf()
                modMessage("Cleared route")
                saveRings()
                loadRings()
            }
            "load" -> {
                val route = args[1]
                config!!.selectedRoute = route
                modMessage("Loaded route $route")
                saveRings()
                loadRings()
            }
            "on" -> {
                config!!.autoP3Active = true
                modMessage("AutoP3 on!")
            }
            "off" -> {
                config!!.autoP3Active = false
                modMessage("AutoP3 off!")
            }
            "save" -> saveRings()
            "loadroute" -> loadRings()
            else -> modMessage("Invalid argument!")
        }
    }
}
}

object RingManager {
    var rings: MutableList<Ring> = mutableListOf()
    var allrings: MutableList<Ring> = mutableListOf()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("config/catgirlyharim/rings.json")

    fun loadRings() {
        if (file.exists()) {
            allrings = gson.fromJson(file.readText(), object : TypeToken<List<Ring>>() {}.type)
            rings = allrings.filter { it.route == config!!.selectedRoute }.toMutableList()
        }
    }

    fun saveRings() {
        val filteredRings = allrings.map { ring ->
            ring.copy(
                delay = ring.delay.takeUnless { it == 1000 },
                lookX = ring.lookX.takeUnless { it == 1000.0 },
                lookY = ring.lookY.takeUnless { it == 1000.0 },
                lookZ = ring.lookZ.takeUnless { it == 1000.0 },
                depth = ring.depth.takeUnless { it == 1000f },
                delaying = ring.delaying.takeUnless { it == false },
                stopping = ring.stopping.takeUnless { it == false },
                walking = ring.walking.takeUnless { it == false },
                looking = ring.looking.takeUnless { it == false },
                silent = ring.silent.takeUnless { it == false },
            )
        }
        file.writeText(gson.toJson(filteredRings))
    }

    @SubscribeEvent
    fun onLoad(event: WorldEvent.Load) {
        loadRings()
    }
}
data class Ring(
    /*Hi :3*/
    val type: String,
    var active: Boolean,
    var route: String,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float,
    var pitch: Float,
    var height: Float,
    var width: Float,
    var lookX: Double? = null,
    var lookY: Double? = null,
    var lookZ: Double? = null,
    var depth: Float? = null,
    var stopping: Boolean? = null,
    var looking: Boolean? = null,
    var walking: Boolean? = null,
    var silent: Boolean? = null,
    var delaying: Boolean? = null,
    var delay: Int? = null
)