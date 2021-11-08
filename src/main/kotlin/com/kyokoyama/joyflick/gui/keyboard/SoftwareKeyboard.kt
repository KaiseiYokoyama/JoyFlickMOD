package com.kyokoyama.joyflick.gui.keyboard

import com.kyokoyama.joyflick.JoyFlick
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.gui.drawCenteredChar
import com.kyokoyama.joyflick.gui.drawRectangle
import com.kyokoyama.joyflick.gui.keyboard.common.Entry
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import com.mrcrayfish.controllable.client.Buttons
import com.mrcrayfish.controllable.client.RenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.widget.Widget
import net.minecraft.util.ResourceLocation
import org.lwjgl.glfw.GLFWGamepadState
import java.awt.Color

interface SoftwareKeyboard {

    val keySize: Float
    val keyboardHeight: Float
    val hintsHeight: Float
        get() = 7f * if (actions.isNotEmpty()) {
            2f
        } else {
            1f
        }
    val padding: Float
        get() = 1f
    val areaHeight: Float
        get() = padding + hintsHeight + padding + keyboardHeight

    /**
     * そのソフトウェアキーボードに固有の操作の説明
     */
    val actions: Array<Action>

    /**
     * いずれのソフトウェアキーボードにも共通する操作の説明
     */
    private val commonActions: Array<Action>
        get() = arrayOf(
            Action("←", Buttons.LEFT_BUMPER),
            Action("→", Buttons.RIGHT_BUMPER),
            Action("削除", Buttons.A),
            Action("切替", Buttons.SELECT),
            Action("送信", Buttons.START),
        )

    /**
     * ソフトウェアキーボードに対して行える操作を表すクラス
     */
    class Action(
        val description: String,
        /**
         * `Controllable`の`Buttons`に定義されている定数を用いて指定すること
         * `GLFW`に定義されている変数を使うと、意図したものと違うボタンが出てくるので注意
         */
        val buttons: Array<Int>,
    ) {
        companion object {
            private val BUTTON_IMAGES: ResourceLocation = RenderEvents.CONTROLLER_BUTTONS
            private const val PADDING_WIDTH = 5
            const val SPACING_WIDTH = 10
            const val HEIGHT = 13
            const val ICON_SIZE = HEIGHT
        }

        constructor(description: String, vararg buttons: Int) : this(description, buttons.toTypedArray())

        private val mc = Minecraft.getInstance()

        val width: Float
            get() = buttonWidth * buttons.size + descriptionWidth

        private val buttonWidth: Float
            get() = (PADDING_WIDTH + ICON_SIZE).toFloat()

        private val descriptionWidth: Float
            get() = (PADDING_WIDTH
                    + mc.fontRenderer.getStringWidth(description)
                    + SPACING_WIDTH).toFloat()


        fun render(matrixStack: MatrixStack) {
            // 背景を描画する
//            drawRectangle(matrixStack, 0f, 0f, width, HEIGHT.toFloat()) {
//                it.color(1f, 1f, 1f, 1f)
//            }

            renderButtons(matrixStack)
            renderDescription(matrixStack)
        }

        private fun renderButtons(matrixStack: MatrixStack) {
            val mc = Minecraft.getInstance()
            mc.getTextureManager().bindTexture(BUTTON_IMAGES)

            for (button in buttons) {
                val texU = button * 13f
                val texV = JoyFlick.CONTROLLER.ordinal * 13f

//                val x = 5
//                val y = mc.mainWindow.scaledHeight - size - 5

                RenderSystem.color4f(1f, 1f, 1f, 1f)
                RenderSystem.disableLighting()

                Widget.blit(matrixStack, 0, 0, texU, texV, ICON_SIZE, ICON_SIZE, 256, 256)

                // 座標系を移動させる
                matrixStack.translate(buttonWidth.toDouble(), 0.0, 0.0)
            }
        }

        private fun renderDescription(matrixStack: MatrixStack) {
            val mc = Minecraft.getInstance()

            mc.fontRenderer.drawString(
                // アイコンとフォントを上下中央揃えに見せるため、3f下にずらす
                matrixStack, description, 0f, 3f, Color.BLACK.rgb
            )

            // 座標系を移動させる
            matrixStack.translate(descriptionWidth.toDouble(), 0.0, 0.0)
        }
    }

    /**
     * コントローラの状態を受け取ったときの挙動
     */
    fun onEntry(entry: GLFWGamepadState): Output?

    /**
     * ソフトウェアキーボードの描画
     */
    fun render(ctx: RenderContext)

    fun parse(entry: GLFWGamepadState): Entry

    fun renderHints() {
        val mc = Minecraft.getInstance()

        val matrixStack = MatrixStack()

        // 座標系を画面最下部に移動
        val y = mc.mainWindow.scaledHeight - hintsHeight - padding
        val xPadding = 2.0
        matrixStack.translate(xPadding, y.toDouble(), 0.0)

        // 表示倍率をかける
        val scale = hintsHeight / (Action.HEIGHT * if (actions.isNotEmpty()) {
            2 // 2段分
        } else {
            1
        })
        matrixStack.scale(scale, scale, 1f)

        // 背景を描画
        val actionsWidth = actions.map { it.width }.sum() - Action.SPACING_WIDTH
        val commonActionsWidth = commonActions.map { it.width }.sum() - Action.SPACING_WIDTH
        val (width, height) = Pair(
            actionsWidth.coerceAtLeast(commonActionsWidth),
            Action.HEIGHT.toFloat() * if (actions.isNotEmpty()) {
                2f // 2段分
            } else {
                1f // 1段分
            }
        )
        drawRectangle(matrixStack, 0f, 0f, width, height) {
            it.color(1f, 1f, 1f, 1f)
        }

        // 上段に固有の操作を描画
        for (action in actions) {
            action.render(matrixStack)
        }

        // 下段に共通の操作を描画
        matrixStack.translate(
            -actionsWidth.toDouble() - Action.SPACING_WIDTH,
            if (actions.isNotEmpty()) {
                Action.HEIGHT.toDouble()
            } else {
                0.0
            },
            0.0
        )
        for (action in commonActions) {
            action.render(matrixStack)
        }
    }

    fun renderKey(matrixStack: MatrixStack, x: Int, y: Int, c: Char, isSelected: Boolean) {
        matrixStack.push()

        val fontRenderer = Minecraft.getInstance().fontRenderer

        // 表示位置を指定
        matrixStack.translate(
            keySize * x.toDouble(),
            keySize * y.toDouble(),
            0.0
        )

        RenderSystem.disableTexture()
        RenderSystem.enableBlend()
        RenderSystem.disableAlphaTest()
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableCull()

        // 白背景を描写
        drawRectangle(
            matrixStack,
            padding, padding,
            keySize - 2f * padding,
            keySize - 2f * padding
        ) {
            if (isSelected) {
                it.color(0x90, 0xca, 0xf9, 0xff)
            } else {
                it.color(1f, 1f, 1f, 1f)
            }
        }

        RenderSystem.disableBlend()
        RenderSystem.enableAlphaTest()
        RenderSystem.enableTexture()
        RenderSystem.enableCull()

        // 文字を描写

        // フォントサイズを指定（拡大する）
        val size = keySize * if (isSelected) {
            0.9
        } else {
            0.7
        }
        // 文字の色を指定
        val color = if (isSelected) {
            net.minecraft.util.text.Color.fromHex("#ffffff")!!.color
        } else {
            net.minecraft.util.text.Color.fromHex("#9e9e9e")!!.color
        }
        val mgn = size.toFloat() / fontRenderer.FONT_HEIGHT.toFloat()

        drawCenteredChar(
            matrixStack,
            fontRenderer,
            c,
            keySize.toInt() / 2,
            keySize.toInt() / 2,
            color,
            mgn
        )
        matrixStack.pop()
    }

    fun isConversionActive(): Boolean

    fun tick() {}
}