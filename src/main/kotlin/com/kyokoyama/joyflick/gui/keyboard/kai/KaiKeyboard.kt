package com.kyokoyama.joyflick.gui.keyboard.kai

import com.kyokoyama.joyflick.gui.*
import com.kyokoyama.joyflick.gui.keyboard.RenderContext
import com.kyokoyama.joyflick.gui.keyboard.SoftwareKeyboard
import com.kyokoyama.joyflick.gui.keyboard.common.Entry
import com.kyokoyama.joyflick.gui.keyboard.joyflick.JoyFlickKeyboard
import com.kyokoyama.joyflick.gui.keyboard.joyflick.State
import com.kyokoyama.joyflick.core.kana.Consonant
import com.kyokoyama.joyflick.core.kana.KanaTable
import com.kyokoyama.joyflick.core.kana.Vowel
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.Minecraft
import net.minecraft.util.text.Color
import org.lwjgl.glfw.GLFWGamepadState
import java.lang.IllegalStateException

class KaiKeyboard(override val keySize: Float) : SoftwareKeyboard {
    private val joyFlick = JoyFlickKeyboard(0f)

    override val keyboardHeight: Float
        get() = keySize * 2f

    override val actions: Array<SoftwareKeyboard.Action>
        get() = joyFlick.actions

    override fun onEntry(entry: GLFWGamepadState): Output? = joyFlick.onEntry(entry)

    override fun parse(entry: GLFWGamepadState): Entry = joyFlick.parse(entry)

    override fun isConversionActive(): Boolean = joyFlick.isConversionActive()

    override fun render(ctx: RenderContext) {
        // この一行がないと、なぜか操作説明の背景が描画されない
        joyFlick.render(ctx)

        val matrixStack = MatrixStack()
        val mc = Minecraft.getInstance()
        val state = joyFlick.state

        // 入力した文字のぶん、右にずらす
        val offsetX = ctx.textWidth - (2.0 + keySize)
        if (offsetX > 0) {
            matrixStack.translate(offsetX, 0.0, 0.0)
        }

        fun dots(
            source: MutableList<Pair<Int, Int>>,
            f: (MutableList<Pair<Int, Int>>) -> List<Pair<Int, Int>>
        ): List<Pair<Int, Int>> = f(source)

        fun spread(
            dots: List<Pair<Int, Int>>
        ) = dots.map { Pair(it.first * 2 - 1, it.second * 2 - 1) }

        fun offset(offset: Vector2f): Vector2f = (offset * 1.3f - Vector2f(0.28f, 0.28f)) * keySize

        // 描画位置を決める
        matrixStack.translate(
            2.0 + 2.0 * keySize,
            mc.mainWindow.scaledHeight - areaHeight.toDouble(),
            0.0
        )
        {
            // draw JoyFlick Kai

            // left widget
            matrixStack.translate(
                -keySize.toDouble(),
                keySize.toDouble(),
                0.0
            ) {
                val red: (IVertexBuilder) -> IVertexBuilder = { it.color(193, 39, 45, 255) }

                val dots = dots(
                    mutableListOf(
                        Pair(1, 1), Pair(0, 1), Pair(1, 0), Pair(2, 1), Pair(1, 2)
                    )
                ) { dots ->
                    when (state) {
                        is State.CSelected -> {
                            dots
                        }
                        is State.VSelected -> {
                            val idx = Vowel.values().indexOf(state.vowel)
                            dots.removeAt(idx)

                            if (state.vowel == Vowel.A) {
                                spread(dots)
                            } else {
                                dots
                            }
                        }
                    }
                }

                // render dots
                val (offsetX, offsetY) = Pair(-0.5 * keySize, -0.5 * keySize)
                it.translate(offsetX, offsetY, 0.0) {
                    it.scale(1f / 3f, 1f / 3f, 1f) {
                        renderMatrixDots(it, dots, red)
                    }
                }

                // render key
                when (state) {
                    is State.CSelected -> {
                    }
                    is State.VSelected -> {
                        val offset = when (state.vowel) {
                            Vowel.A -> Vector2f(0f, 0f) // 中心
                            Vowel.I -> Vector2f(-0.5f, 0f) // 左
                            Vowel.U -> Vector2f(0f, -0.5f) // 上
                            Vowel.E -> Vector2f(0.5f, 0f) // 右
                            Vowel.O -> Vector2f(0f, 0.5f) // 下
                        }
                        val (x, y) = offset(offset)
                        renderKey(it, x, y, KanaTable.get(state.consonant, state.vowel), red)
                    }
                }
            }

            // right widget
            matrixStack.translate(
                keySize.toDouble(),
                keySize.toDouble(),
                0.0
            ) {
                val blue: (IVertexBuilder) -> IVertexBuilder = { it.color(0x29, 0xab, 0xe2, 255) }

                val dots = dots(
                    mutableListOf(
                        Pair(0, 0), Pair(1, 0), Pair(2, 0),
                        Pair(0, 1), Pair(1, 1), Pair(2, 1),
                        Pair(0, 2), Pair(1, 2), Pair(2, 2)
                    )
                ) { dots ->
                    val idx = if (state.consonant == Consonant.W) {
                        Consonant.monograph.indexOf(Consonant.N)
                    } else {
                        Consonant.monograph.indexOf(state.consonant)
                    }
                    dots.removeAt(idx)

                    when (state.consonant) {
                        Consonant.N, Consonant.W -> spread(dots)
                        else -> dots
                    }
                }

                // render dots
                val (offsetX, offsetY) = Pair(-0.5 * keySize, -0.5 * keySize)
                it.translate(offsetX, offsetY, 0.0) {
                    it.scale(1f / 3f, 1f / 3f, 1f) {
                        renderMatrixDots(it, dots, blue)
                    }
                }

                // render key
                val up = Vector2f(0f, -0.5f)
                val down = up * -1f
                val left = Vector2f(-0.5f, 0f)
                val right = left * -1f
                val offset = when (state.consonant) {
                    Consonant.A -> left + up
                    Consonant.K -> up
                    Consonant.S -> right + up
                    Consonant.T -> left
                    Consonant.N, Consonant.W -> Vector2f.ZERO
                    Consonant.H -> right
                    Consonant.M -> left + down
                    Consonant.Y -> down
                    Consonant.R -> right + down
                    Consonant.XA, Consonant.G, Consonant.Z, Consonant.D,
                    Consonant.XT, Consonant.B, Consonant.P, Consonant.XY,
                    Consonant.XW -> throw IllegalStateException()
                }
                val (x, y) = offset(offset)
                renderKey(it, x, y, KanaTable.get(state.consonant, Vowel.A), blue)
            }
        }
    }

    fun renderMatrixDots(
        matrixStack: MatrixStack,
        dots: List<Pair<Int, Int>>,
        color: (IVertexBuilder) -> IVertexBuilder,
    ) {
        dots.forEach { dot ->
            renderDot(matrixStack, dot.first, dot.second, color)
        }
    }

    fun renderDot(
        matrixStack: MatrixStack,
        x: Int,
        y: Int,
        color: (IVertexBuilder) -> IVertexBuilder
    ) {
        matrixStack.translate(keySize * (x.toDouble() - 0.25), keySize * (y.toDouble() - 0.25), 0.0) {
            // 背景を描写
            RenderSystem.disableTexture()
            RenderSystem.enableBlend()
            RenderSystem.disableAlphaTest()
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableCull()

            drawDot(it, padding, padding, (keySize - 2f * padding) / 2f, color = color)

            RenderSystem.disableBlend()
            RenderSystem.enableAlphaTest()
            RenderSystem.enableTexture()
            RenderSystem.enableCull()
        }
    }

    fun renderKey(
        matrixStack: MatrixStack,
        x: Float,
        y: Float,
        c: Char,
        color: (IVertexBuilder) -> IVertexBuilder
    ) {
        val fontRenderer = Minecraft.getInstance().fontRenderer
        matrixStack.translate(x.toDouble(), y.toDouble(), 0.0) {
            // 背景を描写
            RenderSystem.disableTexture()
            RenderSystem.enableBlend()
            RenderSystem.disableAlphaTest()
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableCull()

            drawDot(it, padding, padding, (keySize - 2f * padding) / 2f, color = color)

            RenderSystem.disableBlend()
            RenderSystem.enableAlphaTest()
            RenderSystem.enableTexture()
            RenderSystem.enableCull()

            // 文字を描写

            // フォントサイズを指定（拡大する）
            val size = keySize * 0.9
            val mgn = size.toFloat() / fontRenderer.FONT_HEIGHT.toFloat()

            drawCenteredChar(
                it,
                fontRenderer,
                c,
//                keySize.toInt() / 2,
//                keySize.toInt() / 2,
                padding.toInt(), 0,
                Color.fromHex("#ffffff")!!.color,
                mgn
            )
        }
    }

    override fun tick() = joyFlick.tick()
}