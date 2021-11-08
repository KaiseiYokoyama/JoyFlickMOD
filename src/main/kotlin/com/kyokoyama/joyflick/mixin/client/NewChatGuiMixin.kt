package com.kyokoyama.joyflick.mixin.client

import com.kyokoyama.joyflick.gui.SoftwareKeyboardChatScreen
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.AbstractGui
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.NewChatGui
import net.minecraft.util.IReorderingProcessor
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*

/**
 * チャットのログの表示位置を、ソフトウェアキーボードの高さぶん上方向にずらして描画する
 */
@Mixin(NewChatGui::class)
class NewChatGuiMixin {
    @Shadow
    @Final
    private lateinit var field_146247_f: Minecraft

    @Shadow
    @Final
    public lateinit var field_146253_i: List<ChatLine<IReorderingProcessor>?>

    @Shadow
    @Final
    private lateinit var field_238489_i_: Deque<ITextComponent>

    @Shadow
    private var field_146250_j: Int = 0

    @Shadow
    private var field_146251_k: Boolean = false

    @Shadow
    private var field_238490_l_: Long = 0L

    private companion object {
//        @Shadow
//        private fun getLineBrightness(counterIn: Int): Double {
//            throw IllegalStateException("Mixin failed to shadow NewGameChatGui::getLineBrightness()")
//        }

        // Note: 本当は`@Shadow`を使って書きたいが、なぜかうまくいかない
        private fun getLineBrightness(counterIn: Int): Double {
            var d0 = counterIn.toDouble() / 200.0
            d0 = 1.0 - d0
            d0 = d0 * 10.0
            d0 = MathHelper.clamp(d0, 0.0, 1.0)
            return d0 * d0
        }
    }

    @Inject(method = ["func_238492_a_"], at = [At("INVOKE")], cancellable = true, remap = false)
    private fun draw(p_238492_1_: MatrixStack, p_238492_2_: Int, callback: CallbackInfo) {
        if (!this.func_238496_i_()) {
            this.func_238498_k_()
            val i: Int = this.func_146232_i()
            val j = field_146253_i.size
            if (j > 0) {
                val screen = field_146247_f.currentScreen
                // ソフトウェアキーボードが表示されているときは、その高さぶん表示を上にずらす
                val keyboardAreaHeight = if (screen is SoftwareKeyboardChatScreen) {
                    screen.getSoftwareKeyboardAreaHeight()
                } else { 0f }

                var flag = false
                if (this.func_146241_e()) {
                    flag = true
                }
                val d0: Double = this.func_194815_g()
                val k = MathHelper.ceil(this.func_146228_f().toDouble() / d0)
                RenderSystem.pushMatrix()
                // ソフトウェアキーボードの高さのぶん、表示を上にずらす
                RenderSystem.translatef(2.0f, 18.0f - keyboardAreaHeight, 0.0f)
                RenderSystem.scaled(d0, d0, 1.0)
                val d1 = field_146247_f.gameSettings.chatOpacity * 0.9 + 0.1
                val d2 = field_146247_f.gameSettings.accessibilityTextBackgroundOpacity
                val d3 = 9.0 * (field_146247_f.gameSettings.chatLineSpacing + 1.0)
                val d4 = -8.0 * (field_146247_f.gameSettings.chatLineSpacing + 1.0) + 4.0 * field_146247_f.gameSettings.chatLineSpacing
                var l = 0
                var i1 = 0
                while (i1 + field_146250_j < field_146253_i.size && i1 < i) {
                    val chatline = field_146253_i[i1 + field_146250_j]
                    if (chatline != null) {
                        val j1: Int = p_238492_2_ - chatline.updatedCounter
                        if (j1 < 200 || flag) {
                            val d5 = if (flag) 1.0 else getLineBrightness(j1)
                            val l1 = (255.0 * d5 * d1).toInt()
                            val i2 = (255.0 * d5 * d2).toInt()
                            ++l
                            if (l1 > 3) {
                                val j2 = 0
                                val d6 = (-i1).toDouble() * d3
                                p_238492_1_.push()
                                p_238492_1_.translate(0.0, 0.0, 50.0)
                                AbstractGui.fill(p_238492_1_, -2, (d6 - d3).toInt(), 0 + k + 4, d6.toInt(), i2 shl 24)
                                RenderSystem.enableBlend()
                                p_238492_1_.translate(0.0, 0.0, 50.0)
                                field_146247_f.fontRenderer.func_238407_a_(p_238492_1_, chatline.lineString, 0.0f, (d6 + d4).toInt().toFloat(), 16777215 + (l1 shl 24))
                                RenderSystem.disableAlphaTest()
                                RenderSystem.disableBlend()
                                p_238492_1_.pop()
                            }
                        }
                    }
                    ++i1
                }
                if (!field_238489_i_.isEmpty()) {
                    val k2 = (128.0 * d1).toInt()
                    val i3 = (255.0 * d2).toInt()
                    p_238492_1_.push()
                    p_238492_1_.translate(0.0, 0.0, 50.0)
                    AbstractGui.fill(p_238492_1_, -2, 0, k + 4, 9, i3 shl 24)
                    RenderSystem.enableBlend()
                    p_238492_1_.translate(0.0, 0.0, 50.0)
                    field_146247_f.fontRenderer.func_243246_a(p_238492_1_, TranslationTextComponent("chat.queue", field_238489_i_.size), 0.0f, 1.0f, 16777215 + (k2 shl 24))
                    p_238492_1_.pop()
                    RenderSystem.disableAlphaTest()
                    RenderSystem.disableBlend()
                }
                if (flag) {
                    val l2 = 9
                    RenderSystem.translatef(-3.0f, 0.0f, 0.0f)
                    val j3 = j * l2 + j
                    val k3 = l * l2 + l
                    val l3 = field_146250_j * k3 / j
                    val k1 = k3 * k3 / j3
                    if (j3 != k3) {
                        val i4 = if (l3 > 0) 170 else 96
                        val j4 = if (field_146251_k) 13382451 else 3355562
                        AbstractGui.fill(p_238492_1_, 0, -l3, 2, -l3 - k1, j4 + (i4 shl 24))
                        AbstractGui.fill(p_238492_1_, 2, -l3, 1, -l3 - k1, 13421772 + (i4 shl 24))
                    }
                }
                RenderSystem.popMatrix()
            }
        }
        callback.cancel()
    }

    @Shadow
    private fun func_238496_i_(): Boolean {
        throw IllegalStateException("Mixin failed to shadow NewGameChatGui.func_238496_i_()")
    }

    @Shadow
    private fun func_238498_k_() {
        throw IllegalStateException("Mixin failed to shadow NewGameChatGui.func_238498_k_()")
    }

    @Shadow
    public fun func_146232_i(): Int {
        throw IllegalStateException("Mixin failed to shadow NewGameChatGui.getLineCount()")
    }

    @Shadow
    private fun func_146241_e(): Boolean {
        throw IllegalStateException("Mixin failed to shadow NewGameChatGui.getChatOpen()")
    }

    @Shadow
    public fun func_194815_g(): Double {
        throw IllegalStateException("Mixin failed to shadow NewGameChatGui.getScale()")
    }

    @Shadow
    public fun func_146228_f(): Int {
        throw IllegalStateException("Mixin failed to shadow NewGameChatGui.getChatWidth()")
    }
}