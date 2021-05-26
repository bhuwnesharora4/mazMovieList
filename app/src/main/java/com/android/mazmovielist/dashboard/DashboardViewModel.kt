package com.android.mazmovielist.dashboard

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.mazmovielist.data.db.MoviesDao
import com.android.mazmovielist.data.model.MoviesResponse
import com.android.mazmovielist.data.model.ResultsItem
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import java.net.URLConnection

class DashboardViewModel : ViewModel() {

//    data members
    var currentPage: Int = 1
    var moviesList: MutableLiveData<MutableList<ResultsItem>> = MutableLiveData()
    lateinit var navigator: DashboardNavigator
    var totalPages: Int = 100
    private val mIsLoading = ObservableBoolean()
    private val moviesURL =
        "http://api.themoviedb.org/3/movie/popular?api_key=f8c2fb3301267b649b40cb8d22023776"

//    interface to communicate with activity/ui
    interface DashboardNavigator {
        fun addDataToAdapter(entries: MutableList<ResultsItem>)
    }

//    getting loader status
    fun getIsLoading(): ObservableBoolean {
        return mIsLoading
    }

//    setting loader status
    fun setIsLoading(isLoading: Boolean) {
        mIsLoading.set(isLoading)
    }

//    network method to get movies list
    fun getMoviesList() {
        setIsLoading(true)
        viewModelScope.launch {
            val conn: URLConnection =
                URL("$moviesURL&page=$currentPage").openConnection()

            val content = getContent(conn)
            val resp: MoviesResponse? = Gson().fromJson(content, MoviesResponse::class.java)
            setIsLoading(false)
            moviesList.value = resp?.results
            currentPage = resp?.page!!
            totalPages = resp.totalPages ?: 0
        }

    }

//    network call
    private suspend fun getContent(conn: URLConnection): String {
        var result: String
        withContext(Dispatchers.IO) {
            val input: InputStream = conn.getInputStream()
            result = convertStreamToString(input)
        }
        return result
    }

//    converting stream to readable data
    @Throws(UnsupportedEncodingException::class)
    private fun convertStreamToString(input: InputStream): String {
        val reader = BufferedReader(InputStreamReader(input, "UTF-8"))
        val sb = java.lang.StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }

//    getting list from Room DB
    fun getMoviesListFromDB(moviesDao: MoviesDao) {
        viewModelScope.launch {
            val entries = getMoviesFromDB(moviesDao)
            if (entries.isNotEmpty()) {
                currentPage = entries.size / 20
                navigator.addDataToAdapter(entries)
            } else {
                getMoviesList()
            }
        }
    }

//    room db function
    private suspend fun getMoviesFromDB(moviesDao: MoviesDao): MutableList<ResultsItem> {
        setIsLoading(true)
        var list: MutableList<ResultsItem>
        withContext(Dispatchers.Default) {
            list = moviesDao.getAll()
        }
        setIsLoading(false)
        return list
    }

//    adding movies to Room db
    fun addMoviesToDb(moviesList: MutableList<ResultsItem>, moviesDao: MoviesDao) {
        viewModelScope.launch { addMoviesToDB(moviesList, moviesDao) }
    }

//    room db function
    private suspend fun addMoviesToDB(
        moviesList: MutableList<ResultsItem>,
        moviesDao: MoviesDao
    ) {
        withContext(Dispatchers.Default) {
            moviesDao.insert(moviesList)
        }
    }

//    update to Room DB
    fun updateMovie(moviesDao: MoviesDao, item: ResultsItem) {
        viewModelScope.launch {
            updateMovieToDB(moviesDao, item)
        }
    }

//    room db function
    private suspend fun updateMovieToDB(moviesDao: MoviesDao, item: ResultsItem) {
        withContext(Dispatchers.Default) {
            moviesDao.updateMovie(item)
        }
    }

}
