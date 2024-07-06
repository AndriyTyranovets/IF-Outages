package com.github.andriytyranovets.ifoutages.config

import java.time.format.DateTimeFormatter

object Formatter {
    val Time = DateTimeFormatter.ofPattern("HH:mm")
    val Date = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val DateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    val DateTimeCompact = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss")
}