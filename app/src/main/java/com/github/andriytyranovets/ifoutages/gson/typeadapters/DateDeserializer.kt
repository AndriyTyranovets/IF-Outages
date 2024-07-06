package com.github.andriytyranovets.ifoutages.gson.typeadapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDate

class DateDeserializer : JsonDeserializer<LocalDate> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDate
        = LocalDate.parse(json.getAsJsonPrimitive().getAsString())
}