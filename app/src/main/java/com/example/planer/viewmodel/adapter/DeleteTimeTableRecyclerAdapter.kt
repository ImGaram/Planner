package com.example.planer.viewmodel.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.planer.databinding.RecyclerItemDeleteTimeTableBinding
import com.example.planer.model.TimeTableDto
import com.google.firebase.database.FirebaseDatabase

class DeleteTimeTableRecyclerAdapter(
    private val deleteTimeTableList: ArrayList<TimeTableDto>,
    private val deleteTimeTableNumList: ArrayList<String>
): RecyclerView.Adapter<DeleteTimeTableRecyclerAdapter.ViewHolder>() {
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecyclerItemDeleteTimeTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(deleteTimeTableList[position])

        holder.checkBox.setOnClickListener {
            checkBoxEvent(holder.checkBox, position)
        }

        if (deleteTimeTableList[position].deleteAble == true) holder.checkBox.isChecked = true
        else if (deleteTimeTableList[position].deleteAble == false) holder.checkBox.isChecked = false
    }

    override fun getItemCount(): Int {
        return deleteTimeTableList.size
    }

    inner class ViewHolder(val binding: RecyclerItemDeleteTimeTableBinding): RecyclerView.ViewHolder(binding.root) {
        val checkBox = binding.checkBoxCheckTimeTableDelete

        fun setView(data: TimeTableDto) {
            binding.deleteTimeTableDescriptionText.text = data.description
            binding.deleteTimeTableStartTimeText.text = "${data.startTime} > ${data.endTime}"
        }
    }

    private fun checkBoxEvent(checkBox: CheckBox, position: Int) {
        val hash: HashMap<String, Any> = HashMap()

        if (checkBox.isChecked == true) {
            checkBox.isChecked = true
            hash["deleteAble"] = true
            database.getReference("schedules").child(deleteTimeTableNumList[position]).updateChildren(hash)
            deleteTimeTableList[position].deleteAble = true
        } else {
            checkBox.isChecked = false
            hash["deleteAble"] = false
            database.getReference("schedules").child(deleteTimeTableNumList[position]).updateChildren(hash)
            deleteTimeTableList[position].deleteAble = true
        }
    }
}