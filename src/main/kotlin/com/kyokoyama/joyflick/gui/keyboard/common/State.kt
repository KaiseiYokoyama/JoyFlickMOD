package com.kyokoyama.joyflick.gui.keyboard.common

import com.kyokoyama.joyflick.core.softwarekeyboard.Output
import com.kyokoyama.joyflick.core.softwarekeyboard.Output.*

open class State {
    var deleteState: RepeatCycle<Output> = RepeatCycle.Pending(
        30u, 15u, { Delete(it) }
    ) { it.delete }

    var modify: Boolean = false

    var caretMove: Pair<RepeatCycle<Output>, RepeatCycle<Output>> = Pair(
        // Move left
        RepeatCycle.Pending(
            30u,
            15u,
            { CaretMove(CaretMove.Direction.Left, repeated = it) }) {
            it.caretMove?.direction == CaretMove.Direction.Left
        },
        // Move right
        RepeatCycle.Pending(
            30u, 15u,
            { CaretMove(CaretMove.Direction.Right, repeated = it) }
        ) {
            it.caretMove?.direction == CaretMove.Direction.Right
        }
    )

    open fun updateCore(entry: Entry): Output? {
        // 削除
        val (newDeleteState, deleteOutput) = this.deleteState.update(entry)
        this.deleteState = newDeleteState
        if (deleteOutput != null) {
            return deleteOutput
        }

        // 変換
        val modify = this.modify
        this.modify = entry.modify
        if (!modify && entry.modify) {
            return Modify()
        }

        // カーソル移動
        val (moveLeftState, moveRightState) = this.caretMove
        // 左
        val (newMoveLeftState, moveLeftOutput) = moveLeftState.update(entry)
        this.caretMove = Pair(newMoveLeftState, moveRightState)
        if (moveLeftOutput != null) {
            return moveLeftOutput
        }
        //右
        val (newMoveRightState, moveRightOutput) = moveRightState.update(entry)
        this.caretMove = Pair(moveLeftState, newMoveRightState)
        if (moveRightOutput != null) {
            return moveRightOutput
        }

        return null
    }

    /**
     * 長押しなどによる自動リピートの状態
     */
    sealed class RepeatCycle<O>(
        open val wait: UInt,
        open val repeat: UInt,
        open val output: (repeated: Boolean) -> O,
        open val detector: (Entry) -> Boolean
    ) {
        abstract fun update(entry: Entry): Pair<RepeatCycle<O>, O?>

        /**
         * 一時停止中：リピートに必要な操作が一切加えられていない
         */
        data class Pending<O>(
            override val wait: UInt,
            override val repeat: UInt,
            override val output: (repeated: Boolean) -> O,
            override val detector: (Entry) -> Boolean
        ) : RepeatCycle<O>(wait, repeat, output, detector) {
            override fun update(entry: Entry): Pair<RepeatCycle<O>, O?> {
                return if (detector(entry)) {
                    Pair(
                        Waiting(wait, repeat, output, detector), output(false)
                    )
                } else {
                    Pair(Pending(wait, repeat, output, detector), null)
                }
            }
        }

        /**
         * 待機中：リピートに必要な操作が加えられているが、まだリピートには至っていない
         */
        class Waiting<O>(
            override val wait: UInt,
            override val repeat: UInt,
            override val output: (repeated: Boolean) -> O,
            override val detector: (Entry) -> Boolean
        ) : RepeatCycle<O>(wait, repeat, output, detector) {
            var count: UInt = 0u
            override fun update(entry: Entry): Pair<RepeatCycle<O>, O?> {
                return if (detector(entry)) {
                    if (count < wait) {
                        count += 1u
                        Pair(this, null)
                    } else {
                        Pair(
                            Repeating(wait, repeat, output, detector), output(true)
                        )
                    }
                } else {
                    Pair(Pending(wait, repeat, output, detector), null)
                }
            }
        }

        /**
         * 反復中：一定の入力回数ごとに、定められた出力を繰り返す
         */
        class Repeating<O>(
            override val wait: UInt,
            override val repeat: UInt,
            override val output: (repeated: Boolean) -> O,
            override val detector: (Entry) -> Boolean
        ) : RepeatCycle<O>(wait, repeat, output, detector) {
            var count: UInt = 0u
            override fun update(entry: Entry): Pair<RepeatCycle<O>, O?> {
                return if (detector(entry)) {
                    if (count < repeat) {
                        count += 1u
                        Pair(this, null)
                    } else {
                        Pair(
                            Repeating(wait, repeat, output, detector), output(true)
                        )
                    }
                } else {
                    Pair(Pending(wait, repeat, output, detector), null)
                }
            }
        }


    }
}