package com.kyokoyama.joyflick

import com.mrcrayfish.controllable.client.ButtonBinding
import com.mrcrayfish.controllable.client.Buttons
import net.minecraftforge.client.settings.KeyConflictContext

object ButtonBindings {
    val SOCIAL_INTERACTIONS = ButtonBinding(
        Buttons.START, // +ボタン
        "controllable.key.socialInteractions",
        "key.categories.multiplayer",
        KeyConflictContext.UNIVERSAL
    )
    val SWITCH_KEYBOARD = ButtonBinding(
        Buttons.SELECT, // -ボタン
        "controllable.key.switchKeyboard",
        "key.categories.ui",
        KeyConflictContext.GUI
    )
}