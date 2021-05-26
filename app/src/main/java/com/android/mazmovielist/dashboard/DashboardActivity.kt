package com.android.mazmovielist.dashboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.android.mazmovielist.R
import com.android.mazmovielist.data.db.GenreIdsConverter
import com.android.mazmovielist.data.db.MoviesDao
import com.android.mazmovielist.data.db.MoviesDatabase
import com.android.mazmovielist.data.model.ResultsItem
import com.android.mazmovielist.databinding.ActivityDashboardBinding
import com.android.mazmovielist.movieDetail.MovieDetailActivity
import com.android.mazmovielist.utils.PaginationListener
import java.io.Serializable


class DashboardActivity : AppCompatActivity(), DashboardViewModel.DashboardNavigator {

//    data members
    lateinit var binding: ActivityDashboardBinding
    lateinit var viewModel: DashboardViewModel
    private val refreshReqCode = 101
    private lateinit var moviesDao: MoviesDao
    private var isLoading = true
    lateinit var sharedPreferences: SharedPreferences
    private val prefKeyCounts = "prefKeyCounts"
    private lateinit var movieClicked: ResultsItem
    private var clickedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.navigator = this
        setupScreen()

    }

//    init method
    private fun setupScreen() {
        sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        initDb()
        setAdapter()
        observerMoviesList()
        viewModel.getMoviesListFromDB(moviesDao)
    }

//    adapter for recycler view
    private fun setAdapter() {
        with(binding.rvMovies) {
            layoutManager = GridLayoutManager(this@DashboardActivity, 2)
            adapter =
                MoviesAdapter(this@DashboardActivity, object : MoviesAdapter.ItemClickListener {
                    override fun itemClick(item: ResultsItem, position: Int) {
                        clickedPosition = position
                        movieClicked = item
                        checkSharedPrefs(item)
                    }

                    override fun onFavClick(item: ResultsItem) {
//                        item.favorite = !item.favorite
                        viewModel.updateMovie(moviesDao, item)
                    }
                })
        }
        binding.rvMovies.addOnScrollListener(object :
            PaginationListener(binding.rvMovies.layoutManager as GridLayoutManager) {
            override fun loadMoreItems() {
                if (viewModel.currentPage == viewModel.totalPages)
                    isLoading = false
                if (isLoading) {
                    viewModel.currentPage++
                    viewModel.getMoviesList()
                }
            }

            override fun isLastPage(): Boolean {
                return !isLoading
            }

            override fun isLoading(): Boolean {
                return viewModel.getIsLoading().get()
            }

        })
    }

//    checking/storing for number of clickable movies
    private fun checkSharedPrefs(movie: ResultsItem) {
        var storedIds = sharedPreferences.getString(prefKeyCounts, null)
        if (storedIds != null) {
            val storedSets = storedIds.substring(1).split(",")
            when {
                storedSets.contains(movie.id.toString()) -> {
                    navigateToDetailScreen(movie)
                }
                storedSets.size == 3 -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(null)
                    builder.setMessage("Please reach out to us")
                    builder.setPositiveButton(
                        "OK"
                    ) { _, _ -> }
                    builder.setCancelable(false)
                    builder.show()
                }
                else -> {
                    val editor = sharedPreferences.edit()
                    storedIds += ",${movie.id}"
                    editor.putString(prefKeyCounts, storedIds)
                    editor.apply()
                    navigateToDetailScreen(movie)
                }
            }
        } else {
            val editor = sharedPreferences.edit()
            storedIds = ",${movie.id}"
            editor.putString(prefKeyCounts, storedIds)
            editor.apply()
            navigateToDetailScreen(movie)
        }
    }

//    open movies detail screen
    private fun navigateToDetailScreen(movie: ResultsItem) {
        val intent = Intent(this, MovieDetailActivity::class.java)
        intent.putExtra("movie", movie as Serializable)
        startActivityForResult(intent, refreshReqCode)
    }

//    observing live data
    private fun observerMoviesList() {
        val moviesObserver = Observer<MutableList<ResultsItem>> { moviesList ->
            viewModel.addMoviesToDb(moviesList, moviesDao)
            addDataToAdapter(moviesList)
        }
        viewModel.moviesList.observe(this, moviesObserver)
    }

//    init db
    private fun initDb() {
        val db: MoviesDatabase =
            Room.databaseBuilder(application, MoviesDatabase::class.java, "appDB")
                .addTypeConverter(GenreIdsConverter()).build()
        moviesDao = db.moviesDao()
    }

//    populating recycler view
    override fun addDataToAdapter(entries: MutableList<ResultsItem>) {
        (binding.rvMovies.adapter as MoviesAdapter).addItemList(entries)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == refreshReqCode && resultCode == refreshReqCode) {
            movieClicked.favorite = data?.getBooleanExtra("fav", movieClicked.favorite)!!
            (binding.rvMovies.adapter as MoviesAdapter).notifyItemChanged(clickedPosition)
        }
    }

}