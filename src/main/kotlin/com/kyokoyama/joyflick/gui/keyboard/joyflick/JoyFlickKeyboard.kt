package com.kyokoyama.joyflick.gui.keyboard.joyflick

import com.kyokoyama.joyflick.gui.keyboard.RenderContext
import com.kyokoyama.joyflick.core.kana.Consonant
import com.kyokoyama.joyflick.core.kana.KanaTable
import com.kyokoyama.joyflick.core.kana.Vowel
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.gui.keyboard.SoftwareKeyboard
import com.mojang.blaze3d.matrix.MatrixStack
import com.mrcrayfish.controllable.client.Buttons
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFWGamepadState

class JoyFlickKeyboard(override val keySize: Float) : SoftwareKeyboard {
    override val keyboardHeight: Float = keySize * 3f
    var state: State = State.initialState()

    override val actions: Array<SoftwareKeyboard.Action> = arrayOf(
        SoftwareKeyboard.Action(
            "母音", Buttons.LEFT_THUMB_STICK
        ),
        SoftwareKeyboard.Action(
            "子音", Buttons.RIGHT_THUMB_STICK
        ),
        SoftwareKeyboard.Action(
            "小字゛゜", Buttons.LEFT_TRIGGER, Buttons.RIGHT_TRIGGER
        ),
        SoftwareKeyboard.Action(
            "候補選択", Buttons.DPAD_LEFT, Buttons.DPAD_RIGHT
        ),
        SoftwareKeyboard.Action(
            "入力", Buttons.B
        )
    )

    /**
     * コントローラの入力を受け、内部の状態を変更し、出力を返す
     */
    override fun onEntry(entry: GLFWGamepadState): Output? {
        val entry = parse(entry)
        val (newState, output) = state.update(entry)

        try {
            if (output !== null) {
                return output
            }

            return null
        } finally {
            state = newState
        }
    }

    override fun render(ctx: RenderContext) {
        val matrixStack = MatrixStack()
        val mc = Minecraft.getInstance()

        // 描画位置を決める
        matrixStack.translate(
            2.0,
            mc.mainWindow.scaledHeight - areaHeight.toDouble(),
            0.0
        )

        // draw JoyFlick
        // left widget
        kotlin.run {
            val drawVowKey = { x: Int, y: Int, v: Vowel ->
                val c = KanaTable.get(state.consonant, v)
                val selected = when (state) {
                    is State.CSelected -> false
                    is State.VSelected -> (state as State.VSelected).vowel === v
                }
                renderKey(matrixStack, x, y, c, selected)
            }
            drawVowKey(1, 1, Vowel.A)
            drawVowKey(0, 1, Vowel.I)
            drawVowKey(1, 0, Vowel.U)
            drawVowKey(2, 1, Vowel.E)
            drawVowKey(1, 2, Vowel.O)
        }
        // right widget
        kotlin.run {
            val drawConsKey = { x: Int, y: Int, c: Consonant ->
                renderKey(matrixStack, x, y, KanaTable.get(c, Vowel.A), state.consonant == c)
            }
            drawConsKey(4, 0, Consonant.A)
            drawConsKey(5, 0, Consonant.K)
            drawConsKey(6, 0, Consonant.S)
            drawConsKey(4, 1, Consonant.T)
            drawConsKey(
                5, 1, if (state.consonant == Consonant.W) {
                    Consonant.W
                } else {
                    Consonant.N
                }
            )
            drawConsKey(6, 1, Consonant.H)
            drawConsKey(4, 2, Consonant.M)
            drawConsKey(5, 2, Consonant.Y)
            drawConsKey(6, 2, Consonant.R)
        }
    }

    override fun parse(entry: GLFWGamepadState) = Entry(entry)

    override fun isConversionActive(): Boolean = true

    override fun tick() {
        state.tick()
    }
}