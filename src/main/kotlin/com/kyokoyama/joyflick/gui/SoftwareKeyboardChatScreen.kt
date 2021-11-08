package com.kyokoyama.joyflick.gui

import com.kyokoyama.joyflick.Event
import com.kyokoyama.joyflick.JoyFlick
import com.kyokoyama.joyflick.core.chatscreen.Message
import com.kyokoyama.joyflick.core.softwarekeyboard.Keyboard
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.gui.keyboard.*
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.util.text.IFormattableTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.glfw.GLFWGamepadState

/**
 * ソフトウェアキーボードを用いたチャットを行う画面
 * バニラの`ChatScreen`の代替として表示する
 */
class SoftwareKeyboardChatScreen(defaultText: String) : ChatScreen(defaultText) {
    //    private var keyboard = Keyboard.JoyFlick
    var keyboard = JoyFlick.DEFAULT_KEYBOARD
        private set(value) {
            softwareKeyboard = value.widget(20f)
            JoyFlick.DEFAULT_KEYBOARD = value
            field = value
        }
    private var softwareKeyboard: SoftwareKeyboard = keyboard.widget(20f)

    /**
     * 初期化
     * 独自の`TextFieldWidget`を使う
     */
    override fun init() {
        super.init()

        this.inputField = object : TextFieldWidget(
            font,
            4,
            height - 12,
            width - 4,
            12,
            TranslationTextComponent("chat.editBox")
        ) {
            override fun getNarrationMessage(): IFormattableTextComponent {
                return super.getNarrationMessage()
                    .appendString(this@SoftwareKeyboardChatScreen.commandSuggestionHelper.suggestionMessage)
            }
        }
        this.inputField.setMaxStringLength(256)
        this.inputField.setEnableBackgroundDrawing(false)
        this.inputField.text = defaultInputFieldText
        this.inputField.setResponder { p_212997_1_: String? ->
            func_212997_a(
                p_212997_1_
            )
        }
    }

    /**
     * キーボードによる入力（Enterなど）のすべてを無視する
     * 文字入力の無視は`TextFieldWidget.charTyped()`を参照
     */
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return false
    }

    /**
     * コントローラからの入力を受け取って
     * ソフトウェアキーボードに渡す
     *
     * SoftwareKeyboardChatScreenが表示されているとき
     * コントローラの状態を受け取るたびに呼び出される
     * 実装の詳細は、`ControllableMixin.processButtons()`を参照
     */
    fun onControllerEntry(entry: GLFWGamepadState) {
        val output = softwareKeyboard.onEntry(entry) ?: return
        if (output is Output.Executable) (inputField as TextFieldWidget).update(output)
    }

    /**
     * ソフトウェアキーボードの高さ、およびパディングの高さを合わせた値を返す
     *
     * チャットのログおよびテキストボックスを`render`する際、このメソッドの返り値を元に
     * それぞれの描画位置を上方向にずらす
     */
    fun getSoftwareKeyboardAreaHeight(): Float {
        return softwareKeyboard.areaHeight
    }

    /**
     * `getSoftwareKeyboardAreaHeight()`の値を元に
     * テキストボックスの描画位置を上方向にずらす
     *
     * ref. チャットのログの描画位置の変更は、`NewChatGuiMixin.draw()`を参照
     */
    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // TextBox（チャットの入力欄）を上方向にずらす
        matrixStack.translate(0.0, -getSoftwareKeyboardAreaHeight().toDouble() - font.FONT_HEIGHT, 0.0)
        // ソフトウェアキーボードを描画する
        val ctx = RenderContext(
            enteredText = inputField.text,
            bufferedText = (inputField as TextFieldWidget).bufferedTextString(),
        )
        softwareKeyboard.render(ctx)
        // 操作説明を描画する
        softwareKeyboard.renderHints()
        super.render(matrixStack, mouseX, mouseY, partialTicks)
    }

    fun sendMessage() {
        when (inputField) {
            is TextFieldWidget -> {
                val message = inputField.text + (inputField as TextFieldWidget).bufferedTextString()

                // send message
                sendMessage(message)
            }
        }
    }

    /**
     * キーボードを切り替える
     */
    fun switchKeyboard() {
        keyboard = nextKeyboard(keyboard)
    }

    private fun nextKeyboard(keyboard: Keyboard): Keyboard {
        val keyboards = Keyboard.values()
        return keyboards[
                (keyboards.indexOf(keyboard) + 1) % keyboards.size
        ]
    }

    override fun tick() {
        // 変換候補の選択を行っているかどうかの状態を更新する
        (inputField as TextFieldWidget).setConversionIsActive(softwareKeyboard.isConversionActive())

        // ソフトウェアキーボードに時間の更新を通達する
        softwareKeyboard.tick()

        super.tick()
    }
}