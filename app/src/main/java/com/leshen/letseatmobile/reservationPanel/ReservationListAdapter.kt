package com.leshen.letseatmobile.reservationPanel

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leshen.letseatmobile.R
import com.leshen.letseatmobile.restaurantList.RestaurantListModel

class ReservationListAdapter(
    private var reservationList: List<ReservationDTO>,
    private var restaurantList: List<RestaurantListModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ReservationListAdapter.ViewHolder>() {

    private val sizeCountMap = mutableMapOf<Int, Int>()
    private val restaurantMap: Map<Long, RestaurantInfo>

    data class RestaurantInfo(
        val name: String,
        val photoLink: String?
    )

    init {
        restaurantMap = restaurantList.associateBy(
            { it.restaurantId.toLong() },
            { RestaurantInfo(it.restaurantName, it.photoLink) }
        )
    }

    fun updateData(newList: List<ReservationDTO>?) {
        reservationList = newList ?: emptyList()
        countTableSizes()
        notifyDataSetChanged()
    }

    private fun countTableSizes() {
        sizeCountMap.clear()

        reservationList.forEach { reservation ->
            reservation.reservedTables?.forEach { table ->
                val size = table.size
                sizeCountMap[size] = sizeCountMap.getOrDefault(size, 0) + 1
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(reservation: ReservationDTO)
        fun onCancelButtonClick(reservation: ReservationDTO)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tablesTextView: TextView = itemView.findViewById(R.id.ReservedTable)
        val cancelButton: Button = itemView.findViewById(R.id.CancelButton)
        val nameTextView: TextView = itemView.findViewById(R.id.ReservationRestaurantName)
        val restaurantPictureImageView: ImageView = itemView.findViewById(R.id.ReservationRestaurantPicture)

        init {
            itemView.setOnClickListener {
                reservationList.getOrNull(adapterPosition)?.let { reservation ->
                    itemClickListener.onItemClick(reservation)
                }
            }
            cancelButton.setOnClickListener {
                reservationList.getOrNull(adapterPosition)?.let { reservation ->
                    itemClickListener.onCancelButtonClick(reservation)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reservation_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservationList[position]

        val restaurantInfo = restaurantMap[reservation.restaurantId]
        if (restaurantInfo != null) {
            holder.nameTextView.text = restaurantInfo.name
            Log.d("ReservationListAdapter", "Binding data for: ${restaurantInfo.name}")

            Glide.with(holder.itemView.context)
                .load(restaurantInfo.photoLink ?: R.drawable.template_restauracja)
                .placeholder(R.drawable.template_restauracja)
                .error(R.drawable.template_restauracja)
                .centerCrop()
                .into(holder.restaurantPictureImageView)
        } else {
            Log.e("ReservationListAdapter", "Restaurant info not found for restaurantId: ${reservation.restaurantId}")

            holder.nameTextView.text = "Unknown Restaurant"
            Glide.with(holder.itemView.context)
                .load(R.drawable.template_restauracja)
                .placeholder(R.drawable.template_restauracja)
                .error(R.drawable.template_restauracja)
                .centerCrop()
                .into(holder.restaurantPictureImageView)
        }

        val sizeText = generateSizeText(reservation.reservedTables.orEmpty())
        holder.tablesTextView.text = sizeText
    }

    override fun getItemCount(): Int {
        return reservationList.size
    }

    private fun generateSizeText(tableModels: List<ReservedTable>?): String {
        if (tableModels.isNullOrEmpty()) {
            return "Brak stolików"
        }

        val sizeCountMap = mutableMapOf<Int, Int>()

        tableModels.forEach { table ->
            val size = table.size
            sizeCountMap[size] = sizeCountMap.getOrDefault(size, 0) + 1
        }

        return sizeCountMap.entries.joinToString("\n") { (size, count) ->
            "$count stolik ($size os.)"
        }
    }
}
