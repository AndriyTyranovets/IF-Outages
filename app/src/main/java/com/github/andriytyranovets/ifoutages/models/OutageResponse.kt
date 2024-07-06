package com.github.andriytyranovets.ifoutages.models

data class OutageResponse(val  current: CurrentOuttage, val graphs: OutageSchedule)
