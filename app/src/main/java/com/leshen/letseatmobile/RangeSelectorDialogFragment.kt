package com.leshen.letseatmobile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

class RangeSelectorDialogFragment : DialogFragment() {

    interface RangeSelectorListener {
        fun onRangeSelected(range: Float)
    }

    private var rangeSelectorListener: RangeSelectorListener? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var homeViewModel: HomeViewModel

    fun setRangeSelectorListener(listener: RangeSelectorListener) {
        this.rangeSelectorListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("RangeSelectorPrefs", Context.MODE_PRIVATE)
        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_range_selector, null)

        val rangeSeekBar = view.findViewById<SeekBar>(R.id.rangeSeekBar)
        val rangeValueTextView = view.findViewById<TextView>(R.id.rangeValueTextView)
        val buttonApply = view.findViewById<Button>(R.id.buttonApply)

        builder.setView(view)
            .setTitle("Wybór zasięgu")
            .setNegativeButton("Anuluj") { _, _ -> }
            .setCancelable(true)

        val dialog = builder.create()

        val lastSelectedRange = sharedPreferences.getFloat("lastSelectedRange", 1.0f)
        val stepSize = 0.5f
        val maxProgress = (20.0f / stepSize).toInt()  // Max value 20 represents 20 with 0.5 increment

        rangeSeekBar.max = maxProgress
        val progress = (lastSelectedRange / stepSize).toInt()
        rangeSeekBar.progress = progress
        rangeValueTextView.text = "$lastSelectedRange Km"

        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualProgress = progress * stepSize
                rangeValueTextView.text = "$actualProgress Km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val selectedRange = (seekBar?.progress ?: 0) * stepSize
                rangeSelectorListener?.onRangeSelected(selectedRange)

                sharedPreferences.edit()
                    .putFloat("lastSelectedRange", selectedRange)
                    .apply()
                dialog.dismiss()
            }
        })

        buttonApply.setOnClickListener {
            // Handle button click if needed
        }

        return dialog
    }
}
