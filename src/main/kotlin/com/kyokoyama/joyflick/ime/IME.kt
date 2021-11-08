package com.kyokoyama.joyflick.ime

interface IME {
    /**
     * 対象の文字列を変換する
     * 文字列は適当に分割され、それぞれに複数の変換候補が示される
     */
    fun convert(original: String): List<Conversion>
}