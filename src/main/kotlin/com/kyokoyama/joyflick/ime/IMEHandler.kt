package com.kyokoyama.joyflick.ime

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * IMEのハンドラ
 * 変換はときに時間のかかる処理なので、コルーチンに投げる
 *
 * ## 使い方
 *
 * まずはインスタンスを作る
 * `val handler = IMEHandler()`
 *
 * ### 変換したい文字列があるとき
 *
 * 1. 変換したい文字列を送信する: `handler.send("変換したい文字列")`
 * 2. tickごとに`receive()`を叩いて、変換が終わっているかどうか確認: `val conversion = handler.receive()`
 */
class IMEHandler(
    val ime: IME
) {
    // バッファなしのチャンネルを作成
    private val rx = Channel<List<Conversion>>(10)

    // バッファなしのチャンネルを作成
    private val tx = Channel<String>()

    private val worker = GlobalScope.launch {
        while (true) {
            val original = tx.receive()
            val conversion = ime.convert(original)
            rx.send(conversion)
        }
    }

    fun send(original: String) = runBlocking {
        tx.send(original)
    }

    /**
     * 変換結果を受け取る
     * 変換が終わっていないときは、エラーが帰ってくる
     */
    fun tryReceive() = runBlocking {
        rx.tryReceive()
    }

    /**
     * 変換が終わるまで待って、変換結果を受け取る
     */
    fun receive() = runBlocking {
        rx.receive()
    }
}