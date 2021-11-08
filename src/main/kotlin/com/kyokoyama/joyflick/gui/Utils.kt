package com.kyokoyama.joyflick.gui

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import com.mrcrayfish.controllable.client.Controller
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldVertexBufferUploader
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * キーの中の文字を描画する
 */
fun drawCenteredChar(
    matrixStack: MatrixStack,
    fontRenderer: FontRenderer,
    c: Char,
    x: Int,
    y: Int,
    color: Int,
    scale: Float
) {
    matrixStack.scale(scale, scale, 1f)

    val c = c.toString()
    // フォントが左上にちょっと寄るので、右下に下げる
    val offset = 0.5f
    val x = (x.toFloat() / scale - fontRenderer.getStringWidth(c).toFloat() / 2f + offset)
    val y = (y.toFloat() / scale - fontRenderer.FONT_HEIGHT.toFloat() / 2f + offset)

    fontRenderer.drawString(
        matrixStack,
        c, x, y, color
    )
}

/**
 * キーの背景になる四角形を描画する
 */
fun drawRectangle(
    matrixStack: MatrixStack,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: (IVertexBuilder) -> IVertexBuilder
) {
    val buffer = Tessellator.getInstance().buffer
    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
    val matrix = matrixStack.last.matrix

    color(buffer.pos(matrix, x, y, 0f)).endVertex()
    color(buffer.pos(matrix, x, y + height, 0f)).endVertex()
    color(buffer.pos(matrix, x + width, y + height, 0f)).endVertex()
    color(buffer.pos(matrix, x + width, y, 0f)).endVertex()
    buffer.finishDrawing()

    WorldVertexBufferUploader.draw(buffer)
}

/**
 * 円盤を描画する
 */
fun drawDot(
    matrixStack: MatrixStack,
    x: Float,
    y: Float,
    radius: Float,
    division: UInt = 180u,
    color: (IVertexBuilder) -> IVertexBuilder
) {
    val buffer = Tessellator.getInstance().buffer
    buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR)
    val matrix = matrixStack.last.matrix

    repeat(division.toInt()) {
        val arg = 2f * PI.toFloat() * it.toFloat() / division.toFloat()
        val (x, y) = Pair(
            x + radius * cos(arg),
            y + radius * sin(arg)
        )

        color(buffer.pos(matrix, x, y, 0f)).endVertex()
    }

    buffer.finishDrawing()

    WorldVertexBufferUploader.draw(buffer)
}

/**
 * 使われているコントローラがProコントローラーかどうかを返す
 */
fun Controller.isProCon(): Boolean = this.name == "Nintendo Switch Pro Controller"

fun MatrixStack.translate(x: Double, y: Double, z: Double, f: (mStack: MatrixStack) -> Unit) {
    this.push()
    this.translate(x, y, z)
    f(this)
    this.pop()
}

fun MatrixStack.scale(x: Float, y: Float, z: Float, f: (mStack: MatrixStack) -> Unit) {
    this.push()
    this.scale(x, y, z)
    f(this)
    this.pop()
}

data class Vector2f(
    val x: Float, val y: Float
) {
    operator fun plus(b: Vector2f): Vector2f =
        Vector2f(
            this.x + b.x,
            this.y + b.y,
        )

    operator fun minus(b: Vector2f): Vector2f =
        Vector2f(
            this.x - b.x,
            this.y - b.y,
        )

    operator fun times(b: Float): Vector2f =
        Vector2f(
            this.x * b,
            this.y * b
        )

    companion object {
        val ZERO = Vector2f(0f, 0f)
    }
}