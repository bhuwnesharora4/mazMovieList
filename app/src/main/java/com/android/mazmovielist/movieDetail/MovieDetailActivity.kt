package com.android.mazmovielist.movieDetail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.android.mazmovielist.R
import com.android.mazmovielist.dashboard.DashboardViewModel
import com.android.mazmovielist.data.db.GenreIdsConverter
import com.android.mazmovielist.data.db.MoviesDao
import com.android.mazmovielist.data.db.MoviesDatabase
import com.android.mazmovielist.data.model.ResultsItem
import com.android.mazmovielist.databinding.ActivityMovieDetailBinding
import com.bumptech.glide.Glide

class MovieDetailActivity : AppCompatActivity(), MovieDetailViewModel.MovieDetailNavigator {

//    data members
    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var viewModel: MovieDetailViewModel
    private val refreshReqCode = 101
    private lateinit var moviesDao: MoviesDao
    private lateinit var movie: ResultsItem
    private val imageBasePath = "https://image.tmdb.org/t/p/w500"
    private var movieUpdated = false
    private var isFavourite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(MovieDetailViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.navigator = this
        setupScreen()

    }

//    init method
    private fun setupScreen() {
        initDb()
        movie = intent.getSerializableExtra("movie") as ResultsItem
        supportActionBar?.title = movie.title
        updateUI(movie)
    }

//    init room db
    private fun initDb() {
        val db: MoviesDatabase =
            Room.databaseBuilder(application, MoviesDatabase::class.java, "appDB")
                .addTypeConverter(GenreIdsConverter()).build()
        moviesDao = db.moviesDao()
    }

//    updating UI from intent value
    private fun updateUI(movie: ResultsItem) {
        Glide.with(this)
            .load(imageBasePath + "${movie.posterPath}")
            .into(binding.imgMovie)
        if (movie.favorite)
            binding.imgFav.setImageResource(R.drawable.ic_fav_filled)
        else binding.imgFav.setImageResource(R.drawable.ic_fav_border)
        binding.tvDesc.text = movie.overview
    }

    override fun favouriteClicked() {
        if (!movieUpdated)
            movieUpdated = true
        movie.favorite = !movie.favorite
        isFavourite = movie.favorite
        if (isFavourite)
            binding.imgFav.setImageResource(R.drawable.ic_fav_filled)
        else binding.imgFav.setImageResource(R.drawable.ic_fav_border)
        viewModel.updateMovie(moviesDao, movie)
    }

    override fun onBackPressed() {
        if (movieUpdated) {
            val intent = Intent()
            intent.putExtra("fav", isFavourite)
            setResult(refreshReqCode, intent)
            finish()
        } else super.onBackPressed()
    }

}