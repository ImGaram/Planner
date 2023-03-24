package com.example.planer.viewmodel.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.planer.databinding.RecyclerItemNotCompletedListBinding
import com.example.planer.model.PlanDto
import com.example.planer.view.plan.GetPlanActivity
import com.google.firebase.database.FirebaseDatabase

class NotCompletedListRecyclerAdapter(
    private val favoritePlanList: ArrayList<PlanDto>,
    private val planNumberList: ArrayList<String>,
    private val getPlanActivity: GetPlanActivity,
    private val day: String
) : ListAdapter<PlanDto, NotCompletedListRecyclerAdapter.ViewHolder>(diffUtil) {
    private val database = FirebaseDatabase.getInstance()

    companion object {
        val diffUtil = object :DiffUtil.ItemCallback<PlanDto>() {
            override fun areItemsTheSame(oldItem: PlanDto, newItem: PlanDto): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: PlanDto, newItem: PlanDto): Boolean =
                oldItem.hashCode() == newItem.hashCode()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecyclerItemNotCompletedListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(favoritePlanList[position])

        holder.checkBox.setOnClickListener {
            checkBoxEvent(position, holder.checkBox)
            getPlanActivity.getNotCompletedPlan(day)
            getPlanActivity.initPlanRecycler(day)
        }

        if (favoritePlanList[position].doneAble == true) holder.checkBox.isChecked = true
        else if (favoritePlanList[position].doneAble == false) holder.checkBox.isChecked = false
    }

    override fun getItemCount(): Int {
        return favoritePlanList.size
    }

    inner class ViewHolder(val binding: RecyclerItemNotCompletedListBinding): RecyclerView.ViewHolder(binding.root) {
        val checkBox = binding.checkBoxPlanDone

        fun setView(planDto: PlanDto) {
            binding.notCompletedItemTitleText.text = planDto.title
            binding.notCompletedItemDescriptionText.text = planDto.description
            binding.notCompletedItemDayText.text = planDto.dateTime

            when (planDto.category) {
                "plan" -> {
                    binding.notCompletedCategoryColor.setColorFilter(Color.parseColor("#21F59D"))
                }
                "schedule" -> {
                    binding.notCompletedCategoryColor.setColorFilter(Color.parseColor("#B1F521"))
                }
                "other" -> {
                    binding.notCompletedCategoryColor.setColorFilter(Color.parseColor("#F1F521"))
                }
            }
        }
    }

    private fun checkBoxEvent(position: Int, checkBox: CheckBox) {
        val hash: HashMap<String, Any> = HashMap()

        if (checkBox.isChecked == true) {
            hash["doneAble"] = true
            database.getReference("plans").child(planNumberList[position]).updateChildren(hash)
            checkBox.isChecked = true
        } else {
            hash["doneAble"] = false
            database.getReference("plans").child(planNumberList[position]).updateChildren(hash)
            checkBox.isChecked = false
        }
    }
}