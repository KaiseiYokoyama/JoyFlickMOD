package com.kyokoyama.joyflick.gui.keyboard.kanasyllabary

import com.kyokoyama.joyflick.gui.keyboard.RenderContext
import com.kyokoyama.joyflick.core.kana.Consonant
import com.kyokoyama.joyflick.core.kana.KanaTable
import com.kyokoyama.joyflick.core.kana.Vowel
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.gui.keyboard.SoftwareKeyboard
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFWGamepadState

class KanaSyllabaryKeyboard(override val keySize: Float) : SoftwareKeyboard {
    override val keyboardHeight: Float
        get() = keySize * 5f
    var state: State = State.initialize()

    override val actions: Array<SoftwareKeyboard.Action> = arrayOf()

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

        // キーボードを描画
        val consonants = Consonant.monograph
        val vowels = Vowel.values()
        for ((x, cons) in consonants.withIndex()) {
            for ((y, vow) in vowels.withIndex()) {
                val character = KanaTable.get(cons, vow)
                when (state) {
                    is State.FocusOnCharacters -> {
                        val state = state as State.FocusOnCharacters
                        renderKey(matrixStack, x, y, character, state.consonant == cons && state.vowel == vow)
                    }
                    is State.FocusOnCandidates -> {
                        renderKey(matrixStack, x, y, character, false)
                    }
                }
            }
        }
    }

    override fun parse(entry: GLFWGamepadState) = Entry(entry)

    override fun isConversionActive(): Boolean = state is State.FocusOnCandidates
}