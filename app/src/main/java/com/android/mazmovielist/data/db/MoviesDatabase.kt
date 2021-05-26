package com.android.mazmovielist.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.mazmovielist.data.model.ResultsItem

@Database(entities = [ResultsItem::class], version = 1)
@TypeConverters(GenreIdsConverter::class)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
}