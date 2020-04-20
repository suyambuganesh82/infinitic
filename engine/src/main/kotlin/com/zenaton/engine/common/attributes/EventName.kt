package com.zenaton.engine.common.attributes

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class EventName(override val name: String) : Name(name) {
    companion object {
        @JvmStatic @JsonCreator
        fun fromJson(value: String) = EventName(value)
    }
    @JsonValue
    fun toJson() = name
}
