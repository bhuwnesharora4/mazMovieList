package com.android.mazmovielist.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.mazmovielist.R
import com.android.mazmovielist.databinding.ItemMovieBinding
import com.android.mazmovielist.data.model.ResultsItem
import com.bumptech.glide.Glide

class MoviesAdapter(
    private val parent: DashboardActivity,
    private val clickListener: ItemClickListener
) :
    RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

    private val moviesList = arrayListOf<ResultsItem?>()
    private val imageBasePath = "https://image.tmdb.org/t/p/w300"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MoviesAdapter.ViewHolder {
        val binding =
            DataBindingUtil.inflate<ItemMovieBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_movie, parent, false
            )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return moviesList.size
    }

    override fun onBindViewHolder(holder: MoviesAdapter.ViewHolder, position: Int) {
        holder.setData(moviesList[position]!!, position)
    }

    fun addItemList(list: List<ResultsItem?>) {
        moviesList.addAll(list)
        notifyDataSetChanged()
    }

    fun clearList() {
        moviesList.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(
            movie: ResultsItem,
            position: Int
        ) {
            Glide.with(parent)
                .load(imageBasePath + "${movie.posterPath}")
                .into(binding.imgImage)
            binding.tvName.text = movie.title

            if (movie.favorite)
                binding.imgFav.setImageResource(R.drawable.ic_fav_filled)
            else binding.imgFav.setImageResource(R.drawable.ic_fav_border)

            binding.imgFav.setOnClickListener {
                if (movie.favorite) {
                    movie.favorite = false
                    binding.imgFav.setImageResource(R.drawable.ic_fav_border)
                } else {
                    movie.favorite = true
                    binding.imgFav.setImageResource(R.drawable.ic_fav_filled)
                }
                clickListener.onFavClick(movie)
            }

            binding.imgImage.setOnClickListener { clickListener.itemClick(movie, position) }
        }
    }

    interface ItemClickListener {
        fun itemClick(item: ResultsItem, position: Int)
        fun onFavClick(item: ResultsItem)
    }

}

