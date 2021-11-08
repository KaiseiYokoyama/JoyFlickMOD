package com.kyokoyama.joyflick

import net.minecraftforge.eventbus.api.Event

class Event<I>(val inner: I) : Event()