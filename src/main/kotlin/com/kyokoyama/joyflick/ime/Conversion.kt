package com.kyokoyama.joyflick.ime

interface Conversion {
    val original: String
    val candidates: List<String>
}
