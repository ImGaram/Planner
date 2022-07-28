package com.example.planer.viewmodel

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.planer.R
import com.example.planer.databinding.RecyclerItemPlanListBinding
import com.example.planer.model.PlanDto
import com.example.planer.view.plan.GetPlanActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlanListRecyclerAdapter(
    private val planList: ArrayList<PlanDto>,
    private val planUidList: ArrayList<String>,
    private val getPlanActivity: GetPlanActivity,
    private val day: String
) : RecyclerView.Adapter<PlanListRecyclerAdapter.ViewHolder>() {
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanListRecyclerAdapter.ViewHolder {
        val view = RecyclerItemPlanListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(planList[position])

        holder.checkBox.setOnClickListener {
            checkBoxEvent(position, holder.checkBox)
            getPlanActivity.getNotCompletedPlan(day)
        }

        holder.favorite.setOnClickListener {
            favoriteEvent(position, holder.favorite)
            getPlanActivity.getNotCompletedPlan(day)
        }

        if (planList[position].doneAble == true) holder.checkBox.isChecked = true
        else if (planList[position].doneAble == false) holder.checkBox.isChecked = false

        if (planList[position].favorite == true) {
            holder.favorite.setImageResource(R.drawable.favorite_select)
        } else {
            holder.favorite.setImageResource(R.drawable.favorite_unselect)
        }
    }

    override fun getItemCount(): Int {
        return planList.size
    }

    inner class ViewHolder(private val binding: RecyclerItemPlanListBinding): RecyclerView.ViewHolder(binding.root) {
        val checkBox = binding.checkBoxPlanDone
        val favorite = binding.itemFavoriteSelect

        fun bind(item: PlanDto) {
            binding.itemTitleText.text = item.title
            binding.itemDescriptionText.text = item.description
            binding.itemDayText.text = item.dateTime

            when (item.category) {
                "plan" -> {
                    binding.categoryColor.setColorFilter(Color.parseColor("#21F59D"))
                }
                "schedule" -> {
                    binding.categoryColor.setColorFilter(Color.parseColor("#B1F521"))
                }
                "other" -> {
                    binding.categoryColor.setColorFilter(Color.parseColor("#F1F521"))
                }
            }
        }
    }

    private fun checkBoxEvent(position: Int, checkBox: CheckBox) {
        val hash: HashMap<String, Any> = HashMap()

        if (checkBox.isChecked == true) {
            hash["doneAble"] = true
            database.getReference("plans").child(planUidList[position]).updateChildren(hash)
            checkBox.isChecked = true
        } else {
            hash["doneAble"] = false
            database.getReference("plans").child(planUidList[position]).updateChildren(hash)
            checkBox.isChecked = false
        }
    }

    private fun favoriteEvent(position: Int, favorite: ImageButton) {
        val hash: HashMap<String, Any> = HashMap()

        database.getReference("plans").child(planUidList[position]).addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(PlanDto::class.java)

                if (data?.favorite == false) {
                    hash["favorite"] = true
                    database.getReference("plans").child(planUidList[position]).updateChildren(hash)
                    favorite.setImageResource(R.drawable.favorite_select)
                } else {
                    hash["favorite"] = false
                    database.getReference("plans").child(planUidList[position]).updateChildren(hash)
                    favorite.setImageResource(R.drawable.favorite_unselect)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
