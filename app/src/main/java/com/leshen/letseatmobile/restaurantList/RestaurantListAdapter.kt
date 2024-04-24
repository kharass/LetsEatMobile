package com.leshen.letseatmobile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leshen.letseatmobile.R
import com.leshen.letseatmobile.restaurantList.RestaurantListModel

class RestaurantListAdapter(
    var restaurantList: List<RestaurantListModel>,
    var filteredList: List<RestaurantListModel>,
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RestaurantListAdapter.ViewHolder>() {

    fun updateData(newList: List<RestaurantListModel>?) {
        restaurantList = newList ?: emptyList()
        filteredList = restaurantList
        notifyDataSetChanged()
    }

    fun filterByCategory(category: String) {
        filteredList = if (restaurantList.isNotEmpty() && category.isNotEmpty()) {
            restaurantList.filter { it.restaurantCategory == category }
        } else {
            restaurantList
        }
        notifyDataSetChanged()
    }

    fun resetFilters() {
        filteredList = restaurantList
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(restaurantListModel: RestaurantListModel)
        fun onFavoriteButtonClick(restaurantId: Int, isFavorite: Boolean)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val favoriteButton: ImageButton = itemView.findViewById(R.id.listFavoriteButton)
        val nameTextView: TextView = itemView.findViewById(R.id.listRestaurantName)
        val restaurantPictureImageView: ImageView = itemView.findViewById(R.id.listRestaurantPicture)
        val starTextView: TextView = itemView.findViewById(R.id.listRestaurantStar)
        val distanceTextView: TextView = itemView.findViewById(R.id.listRestaurantDistance)
        val timeTextView: TextView = itemView.findViewById(R.id.listRestaurantTime)

        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(filteredList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = filteredList[position]
        holder.nameTextView.text = restaurant.restaurantName
        holder.distanceTextView.text = restaurant.distance
        holder.timeTextView.text = restaurant.openingHours
        if (restaurant.stars == 0.0) {
            holder.starTextView.text = "Brak ocen"
        } else {
            holder.starTextView.text = restaurant.stars.toString()
        }

        Glide.with(holder.itemView.context)
            .load(restaurant.photoLink)
            .placeholder(R.drawable.template_restauracja)
            .error(R.drawable.template_restauracja)
            .centerCrop()
            .into(holder.restaurantPictureImageView)

        // Ustawienie odpowiedniego obrazka na przycisku ulubionych w zależności od statusu ulubionego
        holder.favoriteButton.setImageResource(if (restaurant.isFavorite) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24)

        // Obsługa kliknięcia na przycisk ulubionych
        holder.favoriteButton.setOnClickListener {
            // Odwrócenie statusu ulubionego
            val newFavoriteStatus = !restaurant.isFavorite
            // Aktualizacja obrazka na przycisku ulubionych
            holder.favoriteButton.setImageResource(if (newFavoriteStatus) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24)
            // Wywołanie funkcji interfejsu onItemClick z nowym statusem ulubionego
            itemClickListener.onFavoriteButtonClick(restaurant.restaurantId, newFavoriteStatus)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }
}
