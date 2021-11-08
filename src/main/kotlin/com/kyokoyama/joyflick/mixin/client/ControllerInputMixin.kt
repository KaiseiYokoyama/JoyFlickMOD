package com.kyokoyama.joyflick.mixin.client

import com.kyokoyama.joyflick.ButtonBindings
import com.kyokoyama.joyflick.Event
import com.kyokoyama.joyflick.JoyFlick
import com.kyokoyama.joyflick.core.softwarekeyboard.Keyboard
import com.kyokoyama.joyflick.gui.SoftwareKeyboardChatScreen
import com.mrcrayfish.controllable.client.Controller
import com.mrcrayfish.controllable.client.ControllerInput
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ControllerInput::class)
class ControllerInputMixin {
    @Inject(method = ["handleButtonInput"], at = [At("TAIL")], remap = false)
    fun handleButtonInput(
        controller: Controller,
        button: Int,
        state: Boolean,
        virtual: Boolean,
        callbackInfo: CallbackInfo
    ) {
        if (state) {
            val mc = Minecraft.getInstance()
            val currentScreen = mc.currentScreen
            when {
                ButtonBindings.SOCIAL_INTERACTIONS.isButtonPressed -> when (currentScreen) {
                    // ソフトウェアキーボード用チャット画面を表示
                    null -> {
                        val screen = SoftwareKeyboardChatScreen("")
                        mc.displayGuiScreen(screen)
                    }
                    // ソフトウェアキーボード用チャット画面を非表示
                    is SoftwareKeyboardChatScreen -> {
                        // チャット送信
                        currentScreen.sendMessage()
                        val keyboard = currentScreen.keyboard
                        // チャット用画面を閉じる
                        mc.displayGuiScreen(null)
                    }
                    else -> {
                    }
                }
                ButtonBindings.SWITCH_KEYBOARD.isButtonPressed -> when (currentScreen) {
                    is SoftwareKeyboardChatScreen -> {
                        // ソフトウェアキーボードを切り替え
                        currentScreen.switchKeyboard()
                    }
                    else -> {
                    }
                }
                else -> {
                }
            }
        }
    }
}