package com.android.mazmovielist.movieDetail

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.mazmovielist.data.db.MoviesDao
import com.android.mazmovielist.data.model.ResultsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailViewModel : ViewModel() {

//    data members
    lateinit var navigator: MovieDetailNavigator
    private val mIsLoading = ObservableBoolean()

    interface MovieDetailNavigator {
        fun favouriteClicked()
    }

    fun getIsLoading(): ObservableBoolean {
        return mIsLoading
    }

    fun setIsLoading(isLoading: Boolean) {
        mIsLoading.set(isLoading)
    }

    fun onFavClicked() {
        navigator.favouriteClicked()
    }

//    update to room DB
    fun updateMovie(moviesDao: MoviesDao, movie: ResultsItem) {
        viewModelScope.launch {
            updateMovieToDB(moviesDao, movie)
        }
    }

//    room db method
    private suspend fun updateMovieToDB(moviesDao: MoviesDao, item: ResultsItem) {
        setIsLoading(true)
        withContext(Dispatchers.Default) {
            moviesDao.updateMovie(item)
        }
        setIsLoading(false)
    }

}