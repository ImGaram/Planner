package com.example.planer.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planer.model.PlanDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlanViewModel: ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    var result = MutableLiveData<Boolean>()
    var planDto = PlanDto()

    fun addPlanLogic(title: String, description: String, year: Int, month: Int, day: Int, category: String) {
        val createUser = auth.currentUser?.uid
        val planList = arrayListOf<PlanDto>()

        database.collection("plans").get().addOnCompleteListener {
            if (it.isSuccessful) {
                planList.clear()

                for (data in it.result) {
                    planList.add(data.toObject(PlanDto::class.java))
                }
            }
            Log.d("List", "addOnSuccessListener: ${planList.size}")

            planDto.id = planList.size + 1
            planDto.title = title
            planDto.description = description
            planDto.date = "$year/$month/$day"
            planDto.createUid = createUser
            planDto.category = category

            database.collection("plans").document().set(planDto).addOnCompleteListener { task ->
                result.value = task.isSuccessful
            }
        }
    }
}