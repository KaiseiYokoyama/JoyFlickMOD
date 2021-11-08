package com.kyokoyama.joyflick.gui.keyboard

import com.kyokoyama.joyflick.core.softwarekeyboard.Keyboard
import com.kyokoyama.joyflick.gui.keyboard.joyflick.JoyFlickKeyboard
import com.kyokoyama.joyflick.gui.keyboard.kai.KaiKeyboard
import com.kyokoyama.joyflick.gui.keyboard.kanasyllabary.KanaSyllabaryKeyboard

fun Keyboard.widget(keySize: Float) = when (this) {
    Keyboard.JoyFlick -> JoyFlickKeyboard(keySize)
    Keyboard.KanaSyllabary -> KanaSyllabaryKeyboard(keySize)
}