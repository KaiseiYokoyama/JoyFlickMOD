package com.kyokoyama.joyflick.ime.google

data class Conversion(
    override val original: String,
    override val candidates: List<String>,
) : com.kyokoyama.joyflick.ime.Conversion