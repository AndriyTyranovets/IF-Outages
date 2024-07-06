package com.github.andriytyranovets.ifoutages.models

import java.time.LocalDate
import java.time.LocalDateTime

data class OutageDay(val scheduleApprovedSince: LocalDateTime, val hoursList: List<OutageHour>, val eventDate: LocalDate)
