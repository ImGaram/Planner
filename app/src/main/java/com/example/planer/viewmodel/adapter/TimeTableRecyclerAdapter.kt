package com.example.planer.viewmodel.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planer.databinding.RecyclerItemTimeTableBinding
import com.example.planer.model.TimeDto

class TimeTableRecyclerAdapter(val timeList: ArrayList<TimeDto>): RecyclerView.Adapter<TimeTableRecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecyclerItemTimeTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(timeList[position])
    }

    override fun getItemCount(): Int {
        return timeList.size
    }

    inner class ViewHolder(val binding: RecyclerItemTimeTableBinding): RecyclerView.ViewHolder(binding.root) {
        fun setView(item: TimeDto) {
            binding.itemSetTimeText.text = "${item.startTime} > ${item.endTime}"
            binding.itemTimeDoText.text = item.description
        }
    }
}