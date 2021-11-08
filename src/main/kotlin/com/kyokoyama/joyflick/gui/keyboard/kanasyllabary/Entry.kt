package com.kyokoyama.joyflick.gui.keyboard.kanasyllabary

import com.kyokoyama.joyflick.core.softwarekeyboard.Direction
import com.kyokoyama.joyflick.core.softwarekeyboard.Output.ConversionUpdate.CursorMove
import com.kyokoyama.joyflick.core.softwarekeyboard.Source
import com.kyokoyama.joyflick.gui.keyboard.common.Entry
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState
import kotlin.math.PI

data class Entry private constructor(
    val entry: GLFWGamepadState,
    val input: Boolean,
    val moveTo: Move<Direction>?,
    val candidateMove: Move<CursorMove.Direction>?,
) : Entry(entry) {
    data class Move<D>(
        val direction: D,
        val source: Source
    ) {
        fun <T> map(f: (D) -> T?): Move<T>? {
            return Move(
                direction = f(this.direction) ?: return null,
                source = this.source
            )
        }
    }

    constructor(entry: GLFWGamepadState) : this(
        entry,
        input = input(entry),
        moveTo = moveTo(entry),
        candidateMove = candidateMove(entry),
    )

    companion object {
        /**
         * デッドゾーンの半径
         */
        private const val deadZoneRadius = 0.4

        private fun moveTo(entry: GLFWGamepadState): Move<Direction>? {
            // 十字キー
            val direction = when {
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) -> Direction.Up
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) -> Direction.Down
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) -> Direction.Left
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) -> Direction.Right
                else -> null
            }
            if (direction != null) return Move(direction, Source.DPad)

            // 右スティック
            val rightStick = entry.rightStick()
            if (rightStick.radius >= deadZoneRadius) {
                val direction = when (rightStick.theta / PI * 4f) {
                    in -3f..-1f -> Direction.Down
                    in -1f..1f -> Direction.Right
                    in 1f..3f -> Direction.Up
                    else -> Direction.Left
                }
                return Move(direction, Source.RStick)
            }

            // 左スティック
            val leftStick = entry.leftStick()
            if (leftStick.radius >= deadZoneRadius) {
                val direction = when (leftStick.theta / PI * 4f) {
                    in -3f..-1f -> Direction.Down
                    in -1f..1f -> Direction.Right
                    in 1f..3f -> Direction.Up
                    else -> Direction.Left
                }
                return Move(direction, Source.LStick)
            }

            return null
        }

        private fun input(entry: GLFWGamepadState) = entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_B) // Aボタン

        private fun candidateMove(entry: GLFWGamepadState): Move<CursorMove.Direction>? = moveTo(entry)?.map {
            when (it) {
                Direction.Left -> CursorMove.Direction.Prev
                Direction.Right -> CursorMove.Direction.Next
                else -> null
            }
        }
    }
}