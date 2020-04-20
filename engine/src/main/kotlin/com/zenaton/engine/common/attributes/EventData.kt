package com.zenaton.engine.common.attributes

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class EventData(override val data: ByteArray) : Data(data) {
    companion object {
        @JvmStatic @JsonCreator
        fun fromJson(value: String) = EventData(value.toByteArray(charset = Charsets.UTF_8))
    }
    @JsonValue
    fun toJson() = String(bytes = data, charset = Charsets.UTF_8)
}
