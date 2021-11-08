package com.kyokoyama.joyflick.gui.keyboard.kanasyllabary

import com.kyokoyama.joyflick.core.kana.Consonant
import com.kyokoyama.joyflick.core.kana.Vowel
import com.kyokoyama.joyflick.core.softwarekeyboard.Direction
import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import kotlin.math.max
import kotlin.math.min

sealed class State(
    protected var input: RepeatCycle<Boolean> = RepeatCycle.Pending(30u, 15u, { it }) {
        if (it is Entry) {
            it.input
        } else {
            false
        }
    },
    protected var cursorMoveState: RepeatCycle<Boolean> = RepeatCycle.Pending(30u, 15u, { it }) {
        if (it is Entry) {
            it.moveTo != null
        } else {
            false
        }
    }
) : com.kyokoyama.joyflick.gui.keyboard.common.State() {

    data class FocusOnCharacters(
        val consonant: Consonant,
        val vowel: Vowel,
    ) : State() {
        fun wholeCopy(consonant: Consonant = this.consonant, vowel: Vowel = this.vowel): FocusOnCharacters {
            val oldInput = input
            val oldCursorMoveState = cursorMoveState
            return this.copy(consonant, vowel).apply {
                this.input = oldInput
                this.cursorMoveState = oldCursorMoveState
            }
        }

        override fun update(entry: Entry): Pair<State, Output?> {
            val commonOutput = super.updateCore(entry)
            if (commonOutput != null) {
                return Pair(this, commonOutput)
            }

            // カーソル移動
            val (newCursorMoveState, cursorMoveOutput) = cursorMoveState.update(entry)
            this.cursorMoveState = newCursorMoveState
            if (cursorMoveOutput != null && entry.moveTo != null) {
                val consonants = Consonant.monograph
                val vowels = Vowel.values()

                val consonantPosition = consonants.indexOf(consonant)
                val vowelPosition = vowels.indexOf(vowel)
                return when (entry.moveTo.direction) {
                    Direction.Up -> {
                        // キー配列の最上段からさらに上に入力 -> 変換候補にカーソルを移す
                        if (vowel == Vowel.A) return Pair(
                            FocusOnCandidates(),
                            Output.RecordOnly.KanaSyllabary.FocusOnCandidates(
                                repeated = cursorMoveOutput,
                                source = entry.moveTo.source
                            )
                        )

                        val newVowel = vowels[max(vowelPosition - 1, 0)]
                        Pair(
                            this.wholeCopy(vowel = newVowel),
                            Output.RecordOnly.KanaSyllabary.CursorMoveTo(
                                consonant, newVowel,
                                source = entry.moveTo.source,
                                direction = entry.moveTo.direction,
                                repeated = cursorMoveOutput
                            )
                        )
                    }
                    Direction.Down -> {
                        val newVowel = vowels[min(vowelPosition + 1, vowels.size - 1)]
                        Pair(
                            this.wholeCopy(vowel = newVowel),
                            Output.RecordOnly.KanaSyllabary.CursorMoveTo(
                                consonant, newVowel,
                                source = entry.moveTo.source,
                                direction = entry.moveTo.direction,
                                repeated = cursorMoveOutput
                            )
                        )
                    }
                    Direction.Left -> {
                        val newConsonant = consonants[
                                // 左右はループさせる
                                (consonantPosition + consonants.size - 1) % consonants.size
                        ]
                        Pair(
                            this.wholeCopy(consonant = newConsonant),
                            Output.RecordOnly.KanaSyllabary.CursorMoveTo(
                                newConsonant, vowel,
                                source = entry.moveTo.source,
                                direction = entry.moveTo.direction,
                                repeated = cursorMoveOutput
                            )
                        )
                    }
                    Direction.Right -> {
                        val newConsonant = consonants[
                                // 左右はループさせる
                                (consonantPosition + 1) % consonants.size
                        ]
                        Pair(
                            this.wholeCopy(consonant = newConsonant),
                            Output.RecordOnly.KanaSyllabary.CursorMoveTo(
                                newConsonant, vowel,
                                source = entry.moveTo.source,
                                direction = entry.moveTo.direction,
                                repeated = cursorMoveOutput
                            )
                        )
                    }
                }
            }

            // 入力
            val (newInputState, inputOutput) = this.input.update(entry)
            this.input = newInputState
            if (inputOutput != null) {
                return Pair(this, Output.Char(consonant, vowel, doubleFlicked = false))
            }

            return Pair(this, null)
        }
    }

    class FocusOnCandidates : State() {
        private var candidateMove: Pair<RepeatCycle<Output>, RepeatCycle<Output>> =
            Pair(
                // Move Prev
                RepeatCycle.Pending(
                    30u,
                    15u,
                    {
                        Output.ConversionUpdate.CursorMove(
                            Output.ConversionUpdate.CursorMove.Direction.Prev,
                            repeated = it
                        )
                    }
                ) {
                    if (it is Entry) {
                        it.candidateMove?.let { it.direction == Output.ConversionUpdate.CursorMove.Direction.Prev }
                            ?: false
                    } else false
                },
                // Move Next
                RepeatCycle.Pending(
                    30u,
                    15u,
                    {
                        Output.ConversionUpdate.CursorMove(
                            Output.ConversionUpdate.CursorMove.Direction.Next,
                            repeated = it
                        )
                    }
                ) {
                    if (it is Entry) {
                        it.candidateMove?.let { it.direction == Output.ConversionUpdate.CursorMove.Direction.Next }
                            ?: false
                    } else false
                }
            )

        override fun update(entry: Entry): Pair<State, Output?> {
            val commonOutput = super.updateCore(entry)
            if (commonOutput != null) {
                return Pair(this, commonOutput)
            }

            // キーによる入力に戻る
            if (entry.moveTo?.direction == Direction.Down) {
                val oldInput = input
                val (oldCursorMoveState, cursorMoveOutput) = cursorMoveState.update(entry)
                return Pair(
                    FocusOnCharacters(Consonant.A, Vowel.A).apply {
                        this.input = oldInput
                        this.cursorMoveState = oldCursorMoveState
                    },
                    Output.RecordOnly.KanaSyllabary.FocusOnKeyboard(
                        source = entry.moveTo.source,
                        repeated = cursorMoveOutput == true,
                    )
                )
            }

            // 候補移動
            val (leftState, rightState) = this.candidateMove
            // 左
            val (newLeftState, leftOutput) = leftState.update(entry)
            this.candidateMove = Pair(newLeftState, rightState)
            if (leftOutput != null) {
                return Pair(this, leftOutput)
            }
            // 右
            val (newRightState, rightOutput) = rightState.update(entry)
            this.candidateMove = Pair(leftState, newRightState)
            if (rightOutput != null) {
                return Pair(this, rightOutput)
            }

            // 選択
            val (newInputState, inputOutput) = this.input.update(entry)
            this.input = newInputState
            if (inputOutput != null) {
                return Pair(this, Output.ConversionUpdate.Select(false))
            }

            return Pair(this, null)
        }
    }

    abstract fun update(entry: Entry): Pair<State, Output?>

    companion object {
        fun initialize() = FocusOnCharacters(
            Consonant.A, Vowel.A,
        )
    }
}