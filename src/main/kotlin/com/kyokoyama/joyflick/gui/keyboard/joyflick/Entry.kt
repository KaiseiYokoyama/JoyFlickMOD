package com.kyokoyama.joyflick.gui.keyboard.joyflick

import com.kyokoyama.joyflick.core.kana.Consonant
import com.kyokoyama.joyflick.core.kana.Vowel
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.gui.keyboard.common.Entry
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState
import kotlin.math.PI

data class Entry private constructor(
    val entry: GLFWGamepadState,
    val consonant: Consonant,
    val vowel: Vowel?,
    val candidateMove: Output.ConversionUpdate.CursorMove?,
    val candidateSelect: Boolean,
) : Entry(entry) {
    constructor(entry: GLFWGamepadState) : this(
        entry,
        calcConsonant(entry.rightStick()),
        calcVowel(entry.leftStick()),
        candidateMove(entry),
        candidateSelect(entry),
    )

    companion object {
        /**
         * デッドゾーンの半径
         */
        private const val deadZoneRadius = 0.4

        /**
         * スティックの状態から、選択されている子音を算出する
         */
        private fun calcConsonant(stick: Stick): Consonant {
            if (stick.pressed) {
                return Consonant.W
            }

            if (stick.radius < deadZoneRadius) {
                return Consonant.N
            }

            return when (stick.theta / PI) {
                in -7f / 8f..-5f / 8f -> Consonant.M
                in -5f / 8f..-3f / 8f -> Consonant.Y
                in -3f / 8f..-1f / 8f -> Consonant.R
                in -1f / 8f..1f / 8f -> Consonant.H
                in 1f / 8f..3f / 8f -> Consonant.S
                in 3f / 8f..5f / 8f -> Consonant.K
                in 5f / 8f..7f / 8f -> Consonant.A
                else -> Consonant.T
            }
        }

        /**
         * スティックの状態から、選択されている母音を算出する
         */
        private fun calcVowel(stick: Stick): Vowel? {
            if (stick.pressed) {
                return Vowel.A
            }

            if (stick.radius < deadZoneRadius) {
                return null
            }

            return when (stick.theta / PI) {
                in -3f / 4f..-1f / 4f -> Vowel.O
                in -1f / 4f..1f / 4f -> Vowel.E
                in 1f / 4f..3f / 4f -> Vowel.U
                else -> Vowel.I
            }
        }

        private fun candidateSelect(entry: GLFWGamepadState): Boolean =
            entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_B) // Aボタン

        private fun candidateMove(entry: GLFWGamepadState): Output.ConversionUpdate.CursorMove? {
            return when {
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) ->
                    Output.ConversionUpdate.CursorMove(Output.ConversionUpdate.CursorMove.Direction.Prev)
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) ->
                    Output.ConversionUpdate.CursorMove(Output.ConversionUpdate.CursorMove.Direction.Next)
                else -> null
            }
        }
    }
}