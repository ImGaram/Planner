package com.example.planer.viewmodel.adapter

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

class FavoriteListRecyclerAdapter(
    private val favoritePlanList: ArrayList<PlanDto>,
    private val planNumberList: ArrayList<String>,
    private val getPlanActivity: GetPlanActivity,
    private val day: String
) : RecyclerView.Adapter<FavoriteListRecyclerAdapter.ViewHolder>() {
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecyclerItemPlanListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(favoritePlanList[position])

        holder.checkBox.setOnClickListener {
            checkBoxEvent(position, holder.checkBox)
            getPlanActivity.getNotCompletedPlan(day)
            getPlanActivity.getFavoriteList(favoritePlanList, planNumberList, this, day)
        }

        holder.favorite.setOnClickListener {
            favoriteEvent(position, holder.favorite)
            getPlanActivity.getNotCompletedPlan(day)
        }

        if (favoritePlanList[position].doneAble == true) holder.checkBox.isChecked = true
        else if (favoritePlanList[position].doneAble == false) holder.checkBox.isChecked = false

        if (favoritePlanList[position].favorite == true) {
            holder.favorite.setImageResource(R.drawable.favorite_select)
        } else {
            holder.favorite.setImageResource(R.drawable.favorite_unselect)
        }
    }

    override fun getItemCount(): Int {
        return favoritePlanList.size
    }

    inner class ViewHolder(val binding: RecyclerItemPlanListBinding): RecyclerView.ViewHolder(binding.root) {
        val checkBox = binding.checkBoxPlanDone
        val favorite = binding.itemFavoriteSelect

        fun setView(planDto: PlanDto) {
            binding.itemTitleText.text = planDto.title
            binding.itemDescriptionText.text = planDto.description
            binding.itemDayText.text = planDto.dateTime

            when (planDto.category) {
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
            database.getReference("plans").child(planNumberList[position]).updateChildren(hash)
            checkBox.isChecked = true
        } else {
            hash["doneAble"] = false
            database.getReference("plans").child(planNumberList[position]).updateChildren(hash)
            checkBox.isChecked = false
        }
    }

    private fun favoriteEvent(position: Int, favorite: ImageButton) {
        val hash: HashMap<String, Any> = HashMap()

        database.getReference("plans").child(planNumberList[position]).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(PlanDto::class.java)

                if (data?.favorite == false) {
                    hash["favorite"] = true
                    database.getReference("plans").child(planNumberList[position]).updateChildren(hash)
                    favorite.setImageResource(R.drawable.favorite_select)
                } else {
                    hash["favorite"] = false
                    database.getReference("plans").child(planNumberList[position]).updateChildren(hash)
                    favorite.setImageResource(R.drawable.favorite_unselect)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}