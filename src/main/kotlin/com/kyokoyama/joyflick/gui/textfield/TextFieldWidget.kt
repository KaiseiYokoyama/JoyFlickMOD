package com.kyokoyama.joyflick.gui.keyboard

import com.kyokoyama.joyflick.Event
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.core.bufferedtextwidget.Output as BTOutput
import com.kyokoyama.joyflick.gui.textfield.BufferedText
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.common.MinecraftForge

/**
 * テキストボックス（ソフトウェアキーボード用）
 */
open class TextFieldWidget(
    val fontRenderer: FontRenderer,
    x: Int, y: Int, width: Int, height: Int, title: ITextComponent
) : TextFieldWidget(fontRenderer, x, y, width, height, title) {
    private val bufferedText = BufferedText()

    /**
     * ソフトウェアキーボードの出力を受けて、
     * 状態をアップデート（文字の追加・削除・modify・変換）する
     */
    fun update(update: Output.Executable) {
        val update = bufferedText.update(update)
        when (update) {
            is BTOutput.Return -> {
                val output = update.output
                when (output) {
                    is Output.Delete -> {
                        deleteFromCursor(-1)
                    }
//                    is SoftwareKeyboard.Output.Modify -> {
//                        modifyChars(-1)
//                    }
//                    is SoftwareKeyboard.Output.Char -> {
//                        writeText(update.char)
//                    }
//                    is SoftwareKeyboard.Output.ConversionUpdate -> {
//
//                    }
                    is Output.CaretMove -> when (output.direction) {
                        Output.CaretMove.Direction.Left -> moveCursorBy(-1)
                        Output.CaretMove.Direction.Right -> moveCursorBy(1)
                    }
                }
            }
            is BTOutput.SelectedCandidate -> {
                // 決定されたテキストを追加
                writeText(update.candidate)
            }
            null -> {
                // do nothing
            }
        }
    }

    fun setConversionIsActive(isActive: Boolean) {
        bufferedText.active = isActive
    }

    fun bufferedTextString() = bufferedText.rawText

    /**
     * キーボードによる文字入力を無視する
     */
    override fun charTyped(codePoint: Char, modifiers: Int): Boolean = false

    /**
     * マウスによる入力を無視する
     */
    override fun mouseClicked(p_231044_1_: Double, p_231044_3_: Double, p_231044_5_: Int): Boolean = false

    /**
     * TextFieldWidgetを描画する
     */
    override fun renderButton(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (getVisible()) {
            if (enableBackgroundDrawing) {
                val i = if (this.isFocused) -1 else -6250336
                fill(matrixStack, x - 1, y - 1, x + width + 1, y + height + 1, i)
                fill(matrixStack, x, y, x + width, y + height, -16777216)
            }
            val color = if (isEnabled) enabledColor else disabledColor
            val j = cursorPosition - lineScrollOffset
            var k = selectionEnd - lineScrollOffset
            val s = this.fontRenderer.func_238412_a_(text.substring(lineScrollOffset), this.adjustedWidth)
            val flag = j >= 0 && j <= s.length
            val flag1 = this.isFocused && cursorCounter / 6 % 2 == 0 && flag
            val l = if (enableBackgroundDrawing) x + 4 else x
            val i1 = if (enableBackgroundDrawing) y + (height - 8) / 2 else y
            var j1 = l
            if (k > s.length) {
                k = s.length
            }
            if (!s.isEmpty()) {
                val s1 = if (flag) s.substring(0, j) else s
                j1 = this.fontRenderer.func_238407_a_(
                    matrixStack, textFormatter.apply(s1, lineScrollOffset),
                    l.toFloat(), i1.toFloat(), color
                )
            }

            // 変換対象のテキストを表示
            j1 += bufferedText.renderText(matrixStack, this.fontRenderer, j1, i1, color)

            val flag2 = cursorPosition < text.length || text.length >= getMaxStringLength()
            var k1 = j1
            if (!flag) {
                k1 = if (j > 0) l + width else l
            } else if (flag2) {
                k1 = j1 - 1 * 2
                --j1
            }
            if (!s.isEmpty() && flag && j < s.length) {
                this.fontRenderer.func_238407_a_(
                    matrixStack, textFormatter.apply(s.substring(j), cursorPosition),
                    j1.toFloat(), i1.toFloat(), color
                )
            }
            if (!flag2 && suggestion != null) {
                this.fontRenderer.drawStringWithShadow(
                    matrixStack,
                    suggestion,
                    (k1 - 1).toFloat(),
                    i1.toFloat(),
                    -8355712
                )
            }
            if (flag1) {
                if (flag2) {
                    fill(matrixStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272)
                } else {
//                    this.fontRenderer.drawStringWithShadow(matrixStack, "_", k1.toFloat(), i1.toFloat(), color)
                    val caret = "|"
                    this.fontRenderer.drawStringWithShadow(
                        matrixStack,
                        caret,
                        (k1 - fontRenderer.getStringWidth(caret)).toFloat(),
                        i1.toFloat(),
                        color
                    )
                }
            }
            if (k != j) {
                val l1 = l + this.fontRenderer.getStringWidth(s.substring(0, k))
                drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + 9)
            }

            bufferedText.renderCandidates(matrixStack, fontRenderer, l, i1 + 10)
        }
    }

    override fun tick() {
        super.tick()
        bufferedText.tick()
    }
}