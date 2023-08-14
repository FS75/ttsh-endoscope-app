package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.entities.ScopeStatus
import com.example.myapplication.viewmodel.SharedViewModel

class StatusAdapter(sharedViewModel: SharedViewModel, private var selectedPosition: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var tag = "StatusAdapter: "
    var scopeList: LiveData<ArrayList<ScopeStatus>> = sharedViewModel.ScopeArrayList

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    private var mListener: onItemClickListener? = null

    fun setOnItemClickListener(listener: onItemClickListener?) {
        mListener = listener
    }

    // 3 different view holders for each status
    private inner class StatusOneViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // setting on item click listener
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mListener!!.onItemClick(position)
                    setSelectedPosition(position)
                }
            }
        }
    }

    private inner class StatusTwoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // setting on item click listener
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mListener!!.onItemClick(position)
                    setSelectedPosition(position)
                }
            }
        }
    }

    private inner class StatusThreeViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // setting on item click listener
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mListener!!.onItemClick(position)
                    setSelectedPosition(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // switch case to check which status scope selected
        return when (viewType) {
            1 -> StatusOneViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.cardview_statusone, parent, false)
            )
            2 -> StatusTwoViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.cardview_statustwo, parent, false)
            )
            else -> StatusThreeViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.cardview_statusthree, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return scopeList.value!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // To change background of selected and not selected views & populate serial no
        var scope = scopeList.value?.get(position)
        if (selectedPosition == position) {
            if (scope != null) {
                when (scope.status?.toInt()) {
                    1 -> {
                        holder as StatusOneViewHolder
                        val cardView = (holder).itemView.findViewById<LinearLayout>(R.id.cardviewStatusOne)
                        cardView.setBackgroundResource(R.drawable.selected_item_background)
                        //To populate the serial no
                        var serialNoTextView = holder.itemView.findViewById<TextView>(R.id.serialNoTextView)
                        serialNoTextView.text = scopeList.value?.get(position)?.serialNo

                    }
                    2 -> {
                        holder as StatusTwoViewHolder
                        val cardView = (holder).itemView.findViewById<LinearLayout>(R.id.cardviewStatusTwo)
                        cardView.setBackgroundResource(R.drawable.selected_item_background)
                        //To populate the serial no
                        var serialNoTextView = holder.itemView.findViewById<TextView>(R.id.serialNoTextView)
                        serialNoTextView.text = scopeList.value?.get(position)?.serialNo
                    }
                    else -> {
                        holder as StatusThreeViewHolder
                        val cardView = (holder).itemView.findViewById<LinearLayout>(R.id.cardviewStatusThree)
                        cardView.setBackgroundResource(R.drawable.selected_item_background)
                        //To populate the serial no
                        var serialNoTextView = (holder).itemView.findViewById<TextView>(R.id.serialNoTextView)
                        serialNoTextView.text = scopeList.value?.get(position)?.serialNo
                    }
                }
            }
        } else {
            if (scope != null) {
                when (scope.status?.toInt()) {
                    1 -> {
                        holder as StatusOneViewHolder
                        val cardView = (holder).itemView.findViewById<LinearLayout>(R.id.cardviewStatusOne)
                        cardView.setBackgroundResource(R.drawable.unselected_item_background)
                        //To populate the serial no
                        var serialNoTextView = holder.itemView.findViewById<TextView>(R.id.serialNoTextView)
                        serialNoTextView.text = scopeList.value?.get(position)?.serialNo
                    }
                    2 -> {
                        holder as StatusTwoViewHolder
                        val cardView = (holder).itemView.findViewById<LinearLayout>(R.id.cardviewStatusTwo)
                        cardView.setBackgroundResource(R.drawable.unselected_item_background)
                        //To populate the serial no
                        var serialNoTextView = holder.itemView.findViewById<TextView>(R.id.serialNoTextView)
                        serialNoTextView.text = scopeList.value?.get(position)?.serialNo
                    }
                    else -> {
                        holder as StatusThreeViewHolder
                        val cardView = (holder).itemView.findViewById<LinearLayout>(R.id.cardviewStatusThree)
                        cardView.setBackgroundResource(R.drawable.unselected_item_background)
                        //To populate the serial no
                        var serialNoTextView = holder.itemView.findViewById<TextView>(R.id.serialNoTextView)
                        serialNoTextView.text = scopeList.value?.get(position)?.serialNo
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return scopeList.value?.get(position)?.status!!.toInt()
    }

    // helper method to update the selected position and notify the adapter of the change
    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}
