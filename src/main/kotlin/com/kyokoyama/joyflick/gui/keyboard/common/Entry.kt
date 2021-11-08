package com.kyokoyama.joyflick.gui.keyboard.common

import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState
import kotlin.math.atan2
import kotlin.math.sqrt

open class Entry private constructor(
    val delete: Boolean,
    val modify: Boolean,
    val caretMove: Output.CaretMove?,
) {
    constructor(entry: GLFWGamepadState) : this(
        delete(entry), modify(entry), cursorMove(entry),
    )

    companion object {
        fun GLFWGamepadState.isPressed(button: Int): Boolean =
            buttons(button).toInt() == GLFW.GLFW_PRESS

        fun GLFWGamepadState.leftStick() = Stick(
            axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X),
            axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y),
            isPressed(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB)
        )

        fun GLFWGamepadState.rightStick() = Stick(
            axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X),
            axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y),
            isPressed(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB)
        )

        fun GLFWGamepadState.rTriggerPressed() = (axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) + 1f) / 2f >= 0.5f

        fun GLFWGamepadState.lTriggerPressed() = (axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) + 1f) / 2f >= 0.5f

        // ProControllerではなぜかGLFW.GLFW_GAMEPAD_BUTTON_AにBボタンが割り当てられている
        fun delete(entry: GLFWGamepadState): Boolean =
//                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_B)
            entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_A)

        fun modify(entry: GLFWGamepadState): Boolean = entry.rTriggerPressed() || entry.lTriggerPressed()

        fun cursorMove(entry: GLFWGamepadState): Output.CaretMove? {
            return when (Pair(
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER),
                entry.isPressed(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER),
            )) {
                Pair(true, true), Pair(false, false) -> null
                // Rボタン押下時
                Pair(false, true) -> Output.CaretMove(Output.CaretMove.Direction.Right)
                // Lボタン押下時
                Pair(true, false) -> Output.CaretMove(Output.CaretMove.Direction.Left)
                else -> throw IllegalStateException("Entry.cursorMove() is failed")
            }
        }
    }

    data class Stick(val x: Float, val y: Float, val pressed: Boolean) {
        /**
         * argument between -PI to PI
         */
        val theta: Double
            get() = atan2(-y.toDouble(), x.toDouble())
        val radius: Float = sqrt(x * x + y * y)
    }
}