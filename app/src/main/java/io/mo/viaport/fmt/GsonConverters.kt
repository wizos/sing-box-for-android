package io.mo.viaport.fmt

import androidx.room.TypeConverter
import io.mo.viaport.helper.GsonHelper.gson


object GsonConverters {
    @TypeConverter
    fun toJson(value: Any?): String {
        if (value is Collection<*>) {
            if (value.isEmpty()) return ""
        }
        return gson.toJson(value)
    }

    @TypeConverter
    fun toList(value: String?): List<*> {
        if (value.isNullOrBlank()) return listOf<Any>()
        return gson.fromJson(value, MutableList::class.java)
    }

    @TypeConverter
    fun toSet(value: String?): Set<*> {
        if (value.isNullOrBlank()) return setOf<Any>()
        return gson.fromJson(value, MutableSet::class.java)
    }
}
