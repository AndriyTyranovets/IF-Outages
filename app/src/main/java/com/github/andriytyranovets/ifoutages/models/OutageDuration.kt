package com.github.andriytyranovets.ifoutages.models

import java.time.LocalDateTime
import java.time.Duration

data class OutageDuration(
    val begin: LocalDateTime,
    val end: LocalDateTime,
    val unknownType: Byte? = null
) {
    fun duration(): Short = Duration.between(begin, end).toHours().toShort()

    fun withEnd(end: LocalDateTime): OutageDuration {
        return OutageDuration(this.begin, end, this.unknownType)
    }
}
