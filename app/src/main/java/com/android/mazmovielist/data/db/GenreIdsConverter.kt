package com.android.mazmovielist.data.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.android.mazmovielist.data.model.ResultsItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// for handling custom objects in room
@ProvidedTypeConverter
class GenreIdsConverter {
    @TypeConverter
    fun fromLogs(logs: List<Int?>): String {
        val type = object : TypeToken<List<Int?>>() {}.type
        return Gson().toJson(logs, type)
    }

    @TypeConverter
    fun toLogs(logs: String): List<Int?> {
        val type = object : TypeToken<List<Int?>>() {}.type
        return Gson().fromJson(logs, type)
    }
}