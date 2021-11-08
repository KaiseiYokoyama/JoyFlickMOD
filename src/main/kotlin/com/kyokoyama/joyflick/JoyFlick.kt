package com.kyokoyama.joyflick

import com.kyokoyama.joyflick.core.softwarekeyboard.Keyboard
import com.mrcrayfish.controllable.client.ControllerIcons
import net.minecraftforge.fml.common.Mod
import java.util.logging.Logger

@Mod(JoyFlick.MOD_ID)
class JoyFlick {
    companion object {
        const val MOD_ID = "joyflick"
        const val MOD_NAME = "JoyFlick"
        val LOGGER = Logger.getLogger(MOD_NAME)
        val CONTROLLER = ControllerIcons.SWITCH_CONTROLLER
        val DOUBLEFLICK_threshold = 5u // tick
        var DEFAULT_KEYBOARD: Keyboard = Keyboard.JoyFlick
    }
}