package com.example.planer.viewmodel

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.planer.R
import com.example.planer.databinding.RecyclerItemPlanListBinding
import com.example.planer.model.PlanDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlanListRecyclerAdapter(val day: String) : RecyclerView.Adapter<PlanListRecyclerAdapter.ViewHolder>() {
    private val planList :ArrayList<PlanDto> = arrayListOf()
    private val planUidList = arrayListOf<String>()
    private val fireStore = FirebaseFirestore.getInstance()

    init {
        fireStore.collection("plans").get().addOnCompleteListener {
            if (it.isSuccessful) {
                planList.clear()
                planUidList.clear()

                for (res in it.result) {
                    val item = res.toObject(PlanDto::class.java)
                    if (item.date == day && item.createUid == FirebaseAuth.getInstance().currentUser?.uid) {
                        planList.add(item)
                        planUidList.add(res.id)
                    } else continue
                }
                notifyDataSetChanged()
            } else {
                Log.d("FAIL", "initRecycler: ${it.exception}")
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanListRecyclerAdapter.ViewHolder {
        val view = RecyclerItemPlanListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(planList[position])

        holder.checkBox.setOnClickListener {
            checkBoxEvent(position, holder.checkBox)
        }

        holder.favorite.setOnClickListener {
            favoriteEvent(position, holder.favorite)
        }

        holder.checkBox.isChecked = holder.checkBox.isChecked == true

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
            binding.itemDayText.text = item.date

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
        val tsDoc = fireStore.collection("plans").document(planUidList[position])
        fireStore.runTransaction {
            val planDto = it.get(tsDoc).toObject(PlanDto::class.java)

            if (checkBox.isChecked == true) {
                planDto?.doneAble = true
                checkBox.isChecked = true
            } else {
                planDto?.doneAble = false
                checkBox.isChecked = false
            }

            it.set(tsDoc, planDto!!)
        }
    }

    private fun favoriteEvent(position: Int, favorite: ImageButton) {
        val tsDoc = fireStore.collection("plans").document(planUidList[position])
        fireStore.runTransaction {
            val planDto = it.get(tsDoc).toObject(PlanDto::class.java)

            if (planDto?.favorite == false) {
                planDto.favorite = true
                favorite.setImageResource(R.drawable.favorite_select)
            } else {
                planDto?.favorite = false
                favorite.setImageResource(R.drawable.favorite_unselect)
            }

            it.set(tsDoc, planDto!!)
        }
    }
}
