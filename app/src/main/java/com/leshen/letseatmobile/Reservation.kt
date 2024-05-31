package com.leshen.letseatmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.leshen.letseatmobile.databinding.FragmentReservationBinding
import com.leshen.letseatmobile.reservationPanel.ReservationListAdapter
import com.leshen.letseatmobile.reservationPanel.ReservationDTO
import com.leshen.letseatmobile.restaurantList.Table

class Reservation : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationListAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val viewModel: ReservationViewModel by viewModels()

    private var _binding: FragmentReservationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservationBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val itemClickListener = object : ReservationListAdapter.OnItemClickListener {
            override fun onItemClick(reservation: ReservationDTO) {
            }

            override fun onCancelButtonClick(reservation: ReservationDTO) {

                if (reservation.reservedTables != null && reservation.reservedTables.isNotEmpty()) {
                    val reservedTable = reservation.reservedTables[0]
                    val table = Table(
                        restaurantId = reservation.restaurantId,
                        size = reservedTable.size,
                        tableId = reservedTable.tableId.toInt(),
                        token = reservedTable.token
                    )
                    viewModel.cancelReservation(reservation.reservationId ?: 0L, table)
                } else {
                    Log.e("DATA_ERROR", "No reserved tables found in reservation")
                }
            }
        }

        viewModel.fetchRestaurantList()

        viewModel.restaurants.observe(viewLifecycleOwner, Observer { restaurantList ->
            if (!::adapter.isInitialized) {
                adapter = ReservationListAdapter(emptyList(), restaurantList, itemClickListener)
                recyclerView.adapter = adapter
            }
        })

        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchReservations()
        }

        viewModel.reservations.observe(viewLifecycleOwner, Observer { reservations ->
            if (reservations != null) {
                adapter.updateData(reservations)
                swipeRefreshLayout.isRefreshing = false
            } else {
                showToast("Failed to load reservations.")
            }
        })

        return view
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
