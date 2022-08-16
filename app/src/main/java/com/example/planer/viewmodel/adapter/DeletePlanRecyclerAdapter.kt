package com.example.planer.viewmodel.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.planer.databinding.RecyclerItemDeleteItemListBinding
import com.example.planer.model.PlanDto
import com.google.firebase.database.FirebaseDatabase

class DeletePlanRecyclerAdapter(
    private val deletePlanList: ArrayList<PlanDto>,
    private val deleteNumList: ArrayList<String>,
    ): RecyclerView.Adapter<DeletePlanRecyclerAdapter.ViewHolder>() {
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecyclerItemDeleteItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(deletePlanList[position])

        holder.checkBox.setOnClickListener {
            checkBoxEvent(holder.checkBox, position)
        }

        if (deletePlanList[position].deleteAble == true) holder.checkBox.isChecked = true
        else if (deletePlanList[position].deleteAble == false) holder.checkBox.isChecked = false
    }

    override fun getItemCount(): Int {
        return deletePlanList.size
    }

    inner class ViewHolder(val binding: RecyclerItemDeleteItemListBinding): RecyclerView.ViewHolder(binding.root) {
        val checkBox = binding.checkBoxCheckDelete

        fun setView(data: PlanDto) {
            binding.deleteItemTitleText.text = data.title
            binding.deleteItemDescriptionText.text = data.description
            binding.deleteItemDayText.text = data.date

            when (data.category) {
                "plan" -> {
                    binding.deleteCategoryColor.setColorFilter(Color.parseColor("#21F59D"))
                }
                "schedule" -> {
                    binding.deleteCategoryColor.setColorFilter(Color.parseColor("#B1F521"))
                }
                "other" -> {
                    binding.deleteCategoryColor.setColorFilter(Color.parseColor("#F1F521"))
                }
            }
        }
    }

    private fun checkBoxEvent(checkBox: CheckBox, position: Int) {
        val hash: HashMap<String, Any> = HashMap()

        if (checkBox.isChecked == true) {
            checkBox.isChecked = true
            hash["deleteAble"] = true
            database.getReference("plans").child(deleteNumList[position]).updateChildren(hash)
            deletePlanList[position].deleteAble = true
        } else {
            checkBox.isChecked = false
            hash["deleteAble"] = false
            database.getReference("plans").child(deleteNumList[position]).updateChildren(hash)
            deletePlanList[position].deleteAble = true
        }
    }
}
