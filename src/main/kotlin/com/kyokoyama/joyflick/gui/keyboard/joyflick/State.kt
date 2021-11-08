package com.kyokoyama.joyflick.gui.keyboard.joyflick

import com.kyokoyama.joyflick.JoyFlick
import com.kyokoyama.joyflick.core.kana.Consonant
import com.kyokoyama.joyflick.core.kana.Vowel
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.core.softwarekeyboard.Output.ConversionUpdate

sealed class State(
    open val consonant: Consonant,
    open var candidateSelect: Boolean,
) : com.kyokoyama.joyflick.gui.keyboard.common.State() {
    private var candidateMove: Pair<RepeatCycle<Output>, RepeatCycle<Output>> = Pair(
        // Move Prev
        RepeatCycle.Pending(
            30u, 15u, { ConversionUpdate.CursorMove(ConversionUpdate.CursorMove.Direction.Prev, repeated = it) }
        ) {
            if (it is Entry) {
                it.candidateMove?.let { it.direction == ConversionUpdate.CursorMove.Direction.Prev } ?: false
            } else false
        },
        // Move Next
        RepeatCycle.Pending(
            30u, 15u, { ConversionUpdate.CursorMove(ConversionUpdate.CursorMove.Direction.Next, repeated = it) }
        ) {
            if (it is Entry) {
                it.candidateMove?.let { it.direction == ConversionUpdate.CursorMove.Direction.Next } ?: false
            } else false
        }
    )

    data class CSelected(
        val prevConsonant: PrevConsonant = PrevConsonant(),
        override val consonant: Consonant,
        override var candidateSelect: Boolean,
    ) : State(consonant, candidateSelect) {
        data class PrevConsonant(
            var consonant: Consonant = Consonant.N,
            var count: UInt = 0u,
        ) {
            fun tick() {
                count++
                if (count > JoyFlick.DOUBLEFLICK_threshold) {
                    // reset
                    consonant = Consonant.N
                    count = 0u
                }
            }
        }

        override fun update(entry: Entry): Pair<State, Output?> {
            val commonOutput = super.updateCore(entry)
            if (commonOutput != null) {
                return Pair(this, commonOutput)
            }

            // 母音選択
            if (entry.vowel !== null) {
                return Pair(
                    VSelected(consonant, candidateSelect, entry.vowel),
                    Output.RecordOnly.JoyFlick.SelectVowel(entry.vowel)
                )
            }

            // 子音変更
            if (entry.consonant !== consonant) {
                return when (consonant) {
                    // スティックを倒すとき
                    Consonant.N -> {
                        val state = this.copy(
                            prevConsonant = PrevConsonant(entry.consonant),
                            consonant = entry.consonant
                        )
                        when (entry.consonant) {
                            // 前倒したところと同じところに、一定時間内にもう一度倒したとき
                            prevConsonant.consonant -> {
                                Pair(state, Output.Char(Consonant.XT, Vowel.U, doubleFlicked = true, repeated = false))
                            }
                            else -> {
                                Pair(
                                    state,
                                    Output.RecordOnly.JoyFlick.SelectConsonant(entry.consonant)
                                )
                            }
                        }
                    }
                    // スティックを戻す・回すとき
                    else -> Pair(
                        this.copy(consonant = entry.consonant),
                        Output.RecordOnly.JoyFlick.SelectConsonant(entry.consonant)
                    )
                }
            }

            return Pair(this, null)
        }

        override fun tick() {
            prevConsonant.tick()
        }
    }

    data class VSelected(
        override val consonant: Consonant,
        override var candidateSelect: Boolean,
        val vowel: Vowel,
    ) : State(consonant, candidateSelect) {
        override fun update(entry: Entry): Pair<State, Output?> {
            val commonOutput = super.updateCore(entry)
            if (commonOutput != null) {
                return Pair(this, commonOutput)
            }

            // 文字入力
            if (entry.vowel === null) {
                return Pair(
                    CSelected(consonant = consonant, candidateSelect = candidateSelect),
                    Output.Char(consonant, vowel, doubleFlicked = false)
                )
            }

            // 母音変更
            if (entry.vowel != vowel) {
                return Pair(
                    this.copy(vowel = entry.vowel),
                    Output.RecordOnly.JoyFlick.SelectVowel(entry.vowel)
                )
            }

            return Pair(this, null)
        }
    }

    fun updateCore(entry: Entry): Output? {
        val commonOutput = super.updateCore(entry)
        if (commonOutput != null) {
            return commonOutput
        }

        // 候補選択
        val select = this.candidateSelect
        this.candidateSelect = entry.candidateSelect
        if (!select && entry.candidateSelect) {
            return ConversionUpdate.Select(false)
        }

        // 候補移動
        val (leftState, rightState) = this.candidateMove
        // 左
        val (newLeftState, leftOutput) = leftState.update(entry)
        this.candidateMove = Pair(newLeftState, rightState)
        if (leftOutput != null) {
            return leftOutput
        }
        // 右
        val (newRightState, rightOutput) = rightState.update(entry)
        this.candidateMove = Pair(leftState, newRightState)
        if (rightOutput != null) {
            return rightOutput
        }

        return null
    }

    abstract fun update(entry: Entry): Pair<State, Output?>

    open fun tick() {}

    companion object {
        fun initialState() = CSelected(
            consonant = Consonant.N,
            candidateSelect = false
        )
    }
}