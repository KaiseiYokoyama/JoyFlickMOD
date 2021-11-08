package com.kyokoyama.joyflick

import com.kyokoyama.joyflick.gui.SoftwareKeyboardChatScreen
import com.mrcrayfish.controllable.client.ControllerInput
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class ControllerInput : ControllerInput() {
    @SubscribeEvent(receiveCanceled = true)
    override fun onRenderScreen(event: GuiScreenEvent.DrawScreenEvent.Pre?) {
        if (Minecraft.getInstance().currentScreen !is SoftwareKeyboardChatScreen) {
            super.onRenderScreen(event)
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    override fun onRenderScreen(event: GuiScreenEvent.DrawScreenEvent.Post?) {
        if (Minecraft.getInstance().currentScreen !is SoftwareKeyboardChatScreen) {
            super.onRenderScreen(event)
        }
    }
}