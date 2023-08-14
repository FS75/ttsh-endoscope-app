package com.example.myapplication


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.entities.Scope
import com.example.myapplication.viewmodel.SharedViewModel


class ScheduleAdapter(sharedViewModel: SharedViewModel, private var scheduleList: LiveData<ArrayList<Scope>>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.activity_schedule_item, viewGroup, false)
        return ViewHolder(v)
    }

    private fun setHeaderBackground(view: View) {
        view.setBackgroundResource(R.drawable.table_header_bg)
    }

    private fun setItemBackground(view: View) {
        view.setBackgroundResource(R.drawable.table_item_bg)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var rowPostition = viewHolder.bindingAdapterPosition

        // The header of the schedule table
        if (rowPostition == 0) {
            viewHolder.apply {
                setHeaderBackground(brandTextView)
                setHeaderBackground(serialNumberTextView)
                setHeaderBackground(scopeTypeTextView)
                setHeaderBackground(modelNumberTextView)
                setHeaderBackground(shiftTextView)
                setHeaderBackground(dateTextView)

                brandTextView.text = "Brand"
                serialNumberTextView.text = "Serial Number"
                scopeTypeTextView.text = "Scope Type"
                modelNumberTextView.text = "Model Number"
                shiftTextView.text = "Shift (Day/Night)"
                dateTextView.text = "Date"
            }
        } else  {
            val modal = scheduleList.value!![rowPostition - 1]
            viewHolder.apply {
                setItemBackground(brandTextView)
                setItemBackground(serialNumberTextView)
                setItemBackground(scopeTypeTextView)
                setItemBackground(modelNumberTextView)
                setItemBackground(shiftTextView)
                setItemBackground(dateTextView)

                // Need to match with db exactly the same
                brandTextView.text = modal.brand
                serialNumberTextView.text = modal.serialNo
                scopeTypeTextView.text = modal.type
                modelNumberTextView.text = modal.model
                shiftTextView.text = modal.shift
                dateTextView.text = modal.date

            }
        }
    }

    override fun getItemCount(): Int {
        return scheduleList.value?.size?.plus(1) ?: 1
       // Log.d("schedule adapter", "scheduleList.value?.size?.plus(1) ?: 1 ")

    }

    fun getItem(position: Int): Scope? {
        return if (position > 0 && position <= scheduleList.value?.size ?: 0) {
            scheduleList.value!![position - 1]
        } else {
            null
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val brandTextView: TextView = itemView.findViewById(R.id.textViewBrand)
        val serialNumberTextView: TextView = itemView.findViewById(R.id.textViewSerialNumber)
        val scopeTypeTextView: TextView = itemView.findViewById(R.id.textViewScopeType)
        val modelNumberTextView: TextView = itemView.findViewById(R.id.textViewModelNumber)
        val shiftTextView: TextView = itemView.findViewById(R.id.textViewShift)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)

    }
}