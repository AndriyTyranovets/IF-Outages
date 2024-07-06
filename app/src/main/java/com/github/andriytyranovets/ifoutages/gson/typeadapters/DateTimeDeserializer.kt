package com.github.andriytyranovets.ifoutages.gson.typeadapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime
        = LocalDateTime.from(formatter.parse(json.getAsJsonPrimitive().getAsString()))
}