package com.example.planer.viewmodel.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.planer.R
import com.example.planer.databinding.RecyclerItemMainPlanListBinding
import com.example.planer.model.PlanDto
import com.example.planer.view.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainPlanListRecyclerAdapter(val plans: ArrayList<PlanDto>): RecyclerView.Adapter<MainPlanListRecyclerAdapter.ViewHolder>() {
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecyclerItemMainPlanListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(plans[position])

        holder.checkBox.setOnClickListener {
            checkBoxEvent(position, holder.checkBox)
        }

        holder.favorite.setOnClickListener {
            favoriteEvent(position, holder.favorite)
        }

        if (plans[position].doneAble == true) holder.checkBox.isChecked = true
        else if (plans[position].doneAble == false) holder.checkBox.isChecked = false

        if (plans[position].favorite == true) {
            holder.favorite.setImageResource(R.drawable.favorite_select)
        } else {
            holder.favorite.setImageResource(R.drawable.favorite_unselect)
        }
    }

    override fun getItemCount(): Int {
        return plans.size
    }

    inner class ViewHolder(val binding: RecyclerItemMainPlanListBinding): RecyclerView.ViewHolder(binding.root) {
        val checkBox = binding.checkBoxMainPlanDone
        val favorite = binding.mainItemFavoriteSelect

        fun setItem(item: PlanDto) {
            binding.mainItemTitleText.text = item.title
            binding.mainItemDescriptionText.text = item.description
            binding.mainItemDayText.text = item.date
            binding.mainItemTimeText.text = item.dateTime

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
            database.getReference("plans").child((position+1).toString()).updateChildren(hash)
            checkBox.isChecked = true
        } else {
            hash["doneAble"] = false
            database.getReference("plans").child((position+1).toString()).updateChildren(hash)
            checkBox.isChecked = false
        }
    }

    private fun favoriteEvent(position: Int, favorite: ImageButton) {
        val hash: HashMap<String, Any> = HashMap()

        database.getReference("plans").child((position+1).toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(PlanDto::class.java)

                if (data?.favorite == false) {
                    hash["favorite"] = true
                    database.getReference("plans").child((position+1).toString()).updateChildren(hash)
                    favorite.setImageResource(R.drawable.favorite_select)
                } else {
                    hash["favorite"] = false
                    database.getReference("plans").child((position+1).toString()).updateChildren(hash)
                    favorite.setImageResource(R.drawable.favorite_unselect)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}