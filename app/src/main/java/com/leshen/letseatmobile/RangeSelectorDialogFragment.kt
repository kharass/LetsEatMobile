package com.leshen.letseatmobile

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class RangeSelectorDialogFragment : DialogFragment() {

    interface RangeSelectorListener {
        fun onRangeSelected(range: Float)
    }

    private var rangeSelectorListener: RangeSelectorListener? = null

    fun setRangeSelectorListener(listener: RangeSelectorListener) {
        this.rangeSelectorListener = listener
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

        // Set the step size for the SeekBar to 0.5
        val stepSize = 0.5f
        val maxProgress = (20 / stepSize).toInt()  // Max value 20 represents 10 with 0.5 increment
        rangeSeekBar.max = maxProgress

        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualProgress = progress * stepSize
                rangeValueTextView.text = "$actualProgress Km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Handle start tracking touch if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val selectedRange = (seekBar?.progress ?: 0) * stepSize
                rangeSelectorListener?.onRangeSelected(selectedRange)
                dialog.dismiss()
            }
        })

        buttonApply.setOnClickListener {
            // Handle button click if needed
        }

        return dialog
    }
}