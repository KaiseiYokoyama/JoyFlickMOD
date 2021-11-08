package com.kyokoyama.joyflick.gui.textfield

import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.core.bufferedtextwidget.Output as BTOutput
import com.kyokoyama.joyflick.gui.drawRectangle
import com.kyokoyama.joyflick.ime.IMEHandler
import com.kyokoyama.joyflick.ime.google.GoogleIME
import com.kyokoyama.joyflick.core.kana.Character
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.coroutines.channels.onSuccess
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.IReorderingProcessor
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.Style
import kotlin.math.max

/**
 * 入力が確定していない文字列
 */
class BufferedText {
    private val characters: ArrayList<Character> = arrayListOf()

    private val underlined =
        { s: String ->
            IReorderingProcessor.fromString(s, Style.EMPTY.setUnderlined(true))
        }

    var active = true

    private val empty =
        { s: String ->
            IReorderingProcessor.fromString(s, Style.EMPTY)
        }

    private var cursorPosition = 0
        set(value) {
            field = MathHelper.clamp(value, 0, rawText.length)
        }

    private val ime = IMEHandler(GoogleIME())

    private var conversionState: ConversionState? = null

    val rawText: String
        get() = characters.build()

    fun update(update: Output.Executable): BTOutput? {
        return when (update) {
            is Output.Delete -> if (characters.isNotEmpty()) {
                deleteCharFromCursor(-1)
                null
            } else {
                BTOutput.Return(update)
            }
            is Output.Modify -> {
                modifyChar(cursorPosition)
                null
            }
            is Output.Char -> {
                writeChar(update.char)
                null
            }
            is Output.CaretMove -> if (characters.isNotEmpty()) {
                when (update.direction) {
                    Output.CaretMove.Direction.Left -> moveCursorBy(-1)
                    Output.CaretMove.Direction.Right -> moveCursorBy(1)
                }
                null
            } else {
                BTOutput.Return(update)
            }
            is Output.ConversionUpdate -> {
                val output = conversionState?.update(update)
                if (output != null) {
                    when (output) {
                        null -> null
                        is ConversionState.Output.Selected -> {
                            // 状態を更新
                            conversionState = output.nextState
                            // 変換された文字をバッファから削除
                            characters.subList(0, output.original.length).clear()
                            // カーソルの位置をずらす
                            cursorPosition = max(0, cursorPosition - output.original.length)

                            BTOutput.SelectedCandidate(output.candidate)
                        }
                    }
                } else null
            }
        }
    }

    fun tick() {
        ime.tryReceive().onSuccess {
            conversionState = ConversionState.build(it)
        }
    }

    private fun deleteCharFromCursor(num: Int) {
        if (characters.isNotEmpty()) {
            val i = Util.func_240980_a_(this.rawText, cursorPosition, num)
            val j = Math.min(i, cursorPosition)
            val k = Math.max(i, cursorPosition)

            if (j != k) {
                // 削除を実行
                characters.subList(j, k).clear()
                cursorPosition = j

                ime.send(rawText)
            }
        }
    }

    private fun writeChar(char: Character) {
        characters.add(cursorPosition, char)
        cursorPosition++

        ime.send(rawText)
    }

    private fun modifyChar(num: Int) {
        if (characters.isEmpty()) return

        val c = characters.removeAt(num - 1)
        characters.add(num - 1, c.modified())

        ime.send(rawText)
    }

    private fun moveCursorBy(num: Int) {
        this.cursorPosition = Util.func_240980_a_(rawText, cursorPosition, num)
    }

    fun renderText(matrixStack: MatrixStack, renderer: FontRenderer, x: Int, y: Int, color: Int): Int {
        if (conversionState == null || conversionState!!.isEmpty()) {
            renderer.func_238407_a_(matrixStack, underlined(rawText), x.toFloat(), y.toFloat(), color)
        } else {
            var offset = 0
            conversionState?.conversions?.forEachIndexed { idx, it ->
                val x = x + offset

                if (idx == 0) {
                    RenderSystem.disableTexture()
                    RenderSystem.enableBlend()
                    RenderSystem.disableAlphaTest()
                    RenderSystem.defaultBlendFunc()
                    RenderSystem.disableCull()

                    drawRectangle(
                        matrixStack,
                        x.toFloat(),
                        y.toFloat(),
                        renderer.getStringWidth(it.original).toFloat(),
                        renderer.FONT_HEIGHT.toFloat(),
                    ) {
                        it.color(0x90, 0xca, 0xf9, 0x88)
                    }

                    RenderSystem.disableBlend()
                    RenderSystem.enableAlphaTest()
                    RenderSystem.enableTexture()
                    RenderSystem.enableCull()
                }

                renderer.func_238407_a_(
                    matrixStack,
                    underlined(it.original),
                    x.toFloat(),
                    y.toFloat(),
                    color
                )
                offset += renderer.getStringWidth(it.original)
            }
        }
        return renderer.getStringWidth(rawText.substring(0, cursorPosition)) + 1
    }

    fun renderCandidates(matrixStack: MatrixStack, renderer: FontRenderer, x: Int, y: Int) {
        var offset = 0
        val conversionState = conversionState ?: return
        conversionState.target.candidates.forEachIndexed { idx, candidate ->
            // 背景を描画
            RenderSystem.disableTexture()
            RenderSystem.enableBlend()
            RenderSystem.disableAlphaTest()
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableCull()

            drawRectangle(
                matrixStack,
                (x + offset).toFloat(),
                y.toFloat(),
                renderer.getStringWidth(candidate).toFloat(),
                renderer.FONT_HEIGHT.toFloat(),
            ) {
                if (conversionState.cursor == idx && active) {
                    it.color(0x90, 0xca, 0xf9, 0xff)
                } else {
                    it.color(1f, 1f, 1f, 1f)
                }
            }

            RenderSystem.disableBlend()
            RenderSystem.enableAlphaTest()
            RenderSystem.enableTexture()
            RenderSystem.enableCull()


            // 文字列を描画
            offset = renderer.drawString(
                matrixStack,
                candidate,
                (x + offset).toFloat(),
                y.toFloat(),
                0
            )
        }
    }

    /**
     * Characterの配列から文字列を生成する
     */
    fun List<Character>.build(): String {
        val builder = StringBuilder()
        this.stream().forEach {
            builder.append(it.toChar())
        }
        return builder.toString()
    }
}