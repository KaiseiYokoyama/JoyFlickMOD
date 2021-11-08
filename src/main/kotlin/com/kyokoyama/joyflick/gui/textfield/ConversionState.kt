package com.kyokoyama.joyflick.gui.textfield

import com.kyokoyama.joyflick.core.softwarekeyboard.Output.ConversionUpdate
import com.kyokoyama.joyflick.ime.Conversion
import kotlin.math.max
import kotlin.math.min

class ConversionState private constructor(
    val conversions: List<Conversion>
) {
    var cursor = 0
    val target: Conversion
        get() = conversions[0]

    companion object {
        fun build(conversions: List<Conversion>): ConversionState? {
            return if (conversions.isEmpty()) {
                null
            } else {
                ConversionState(conversions)
            }
        }
    }

    fun update(update: ConversionUpdate): Output? {
        return when (update) {
            is ConversionUpdate.CursorMove -> when (update.direction) {
                ConversionUpdate.CursorMove.Direction.Next -> {
                    cursor = min(target.candidates.size - 1, cursor + 1)
                    null
                }
                ConversionUpdate.CursorMove.Direction.Prev -> {
                    cursor = max(0, cursor - 1)
                    null
                }
            }
            is ConversionUpdate.Select -> {
                Output.Selected(
                    target.original,
                    target.candidates[cursor],
                    build(conversions.subList(1, conversions.size))
                )
            }
        }
    }

    fun isEmpty() = conversions.isEmpty()

    sealed interface Output {
        data class Selected(
            val original: String,
            val candidate: String,
            val nextState: ConversionState?,
        ) : Output
    }
}