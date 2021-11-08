package com.kyokoyama.joyflick.gui.keyboard

import net.minecraft.client.Minecraft

/**
 * ソフトウェアキーボードの描画時に渡すデータ
 */
class RenderContext(
    val enteredText: String,
    val bufferedText: String,
) {
    val mc = Minecraft.getInstance()
    val textWidth = mc.fontRenderer.getStringWidth(enteredText + bufferedText)
}