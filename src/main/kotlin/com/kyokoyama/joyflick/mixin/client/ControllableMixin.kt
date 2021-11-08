package com.kyokoyama.joyflick.mixin.client

import com.kyokoyama.joyflick.ButtonBindings
import com.kyokoyama.joyflick.gui.SoftwareKeyboardChatScreen
import com.kyokoyama.joyflick.gui.isProCon
import com.mrcrayfish.controllable.Controllable
import com.mrcrayfish.controllable.client.*
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Overwrite
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * `Controllable.processButtons()`に、`SoftwareKeyboardChatScreen`表示時用の
 * 処理を追加する
 *
 */
@Mixin(Controllable::class)
class ControllableMixin {
    /**
     * Controllable MODの`ButtonStates.setState()`がprotectedなので
     * JoyFlick MODからも`setState()`が叩けるようにバイパスを用意する
     */
    private class ButtonStates : com.mrcrayfish.controllable.ButtonStates() {
        fun set(button: Int, state: Boolean) {
            setState(button, state)
        }
    }

    private companion object {
        @Shadow
        var controller: Controller? = null

        @Shadow
        var input: ControllerInput? = null
    }

    @Inject(method = ["processButtons"], at = [At(value = "HEAD")])
    private fun processButtonsHead(states: com.mrcrayfish.controllable.ButtonStates, callback: CallbackInfo) {
        // ソフトウェアキーボードが表示されている場合は、コントローラへの入力を横流しする
        val screen = Minecraft.getInstance().currentScreen
        if (screen is SoftwareKeyboardChatScreen) {
            controller?.gamepadState?.let { screen.onControllerEntry(it) }
        }
    }

    /**
     * ソフトウェアキーボード使用中にカーソルが動くのを防ぐため
     * `Controllable`の`ControllerInput`ではなく
     * オリジナルの`ControllerInput`を使うよう設定する
     */
    @Inject(method = ["onClientSetup"], at = [At("TAIL")])
    private fun onClientSetupTail(event: FMLClientSetupEvent, callback: CallbackInfo) {
        MinecraftForge.EVENT_BUS.unregister(input)
        input = com.kyokoyama.joyflick.ControllerInput()
        MinecraftForge.EVENT_BUS.register(input)

        // 既存のキーコンフィグを変更
        com.mrcrayfish.controllable.client.ButtonBindings.PAUSE_GAME.button = Buttons.HOME // ポーズボタンを+ボタンからホームボタンに変更

        // キーコンフィグを追加
        BindingRegistry.getInstance().register(ButtonBindings.SOCIAL_INTERACTIONS)
        BindingRegistry.getInstance().register(ButtonBindings.SWITCH_KEYBOARD)
    }

    /**
     * Pro Controllerを使っているときは
     * A-BボタンおよびX-Yボタンをそれぞれ入れ替える
     */
    @Overwrite(remap = false)
    private fun gatherAndQueueControllerInput() {
        val currentController = controller ?: return

        val states = ButtonStates()
        if (currentController.isProCon()) {
            states.set(Buttons.B, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_A))
            states.set(Buttons.A, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_B))
            states.set(Buttons.Y, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_X))
            states.set(Buttons.X, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_Y))
        } else {
            states.set(Buttons.A, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_A));
            states.set(Buttons.B, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_B));
            states.set(Buttons.X, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_X));
            states.set(Buttons.Y, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_Y));
        }
        states.set(Buttons.SELECT, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_BACK))
        states.set(Buttons.HOME, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE))
        states.set(Buttons.START, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_START))
        states.set(Buttons.LEFT_THUMB_STICK, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB))
        states.set(Buttons.RIGHT_THUMB_STICK, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB))
        states.set(Buttons.LEFT_BUMPER, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER))
        states.set(Buttons.RIGHT_BUMPER, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER))
        states.set(Buttons.LEFT_TRIGGER, currentController.lTriggerValue >= 0.5f)
        states.set(Buttons.RIGHT_TRIGGER, currentController.rTriggerValue >= 0.5f)
        states.set(Buttons.DPAD_UP, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP))
        states.set(Buttons.DPAD_DOWN, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN))
        states.set(Buttons.DPAD_LEFT, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT))
        states.set(Buttons.DPAD_RIGHT, this.getButtonState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT))
        Minecraft.getInstance().enqueue(Runnable { processButtons(states) })
    }

    @Shadow
    private fun getButtonState(buttonCode: Int): Boolean {
        throw IllegalStateException("Mixin failed to shadow Controllable.getButtonState()")
    }

    @Shadow
    private fun processButtons(states: com.mrcrayfish.controllable.ButtonStates) {
        throw IllegalStateException("Mixin failed to shadow Controllable.processButtons()")
    }
}