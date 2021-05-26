package com.android.mazmovielist.data.db

import androidx.room.*
import com.android.mazmovielist.data.model.ResultsItem

@Dao
interface MoviesDao {

    @Query("SELECT * FROM ResultsItem ORDER BY primaryId ASC")
    fun getAll(): MutableList<ResultsItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movies: MutableList<ResultsItem>)

    @Update
    fun updateMovie(movie: ResultsItem)

}