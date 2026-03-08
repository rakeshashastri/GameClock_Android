package com.example.gameclock.models

import kotlinx.serialization.Serializable

@Serializable
enum class DelayType {
    NONE,
    FISCHER,
    BRONSTEIN,
    SIMPLE_DELAY
}
