package com.example.planer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planer.model.PlanDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class PlanViewModel: ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    var result = MutableLiveData<Boolean>()
    var planDto = PlanDto()

    fun addPlanLogic(title: String, description: String, year: Int, month: Int, day: Int, category: String) {
        val createUser = auth.currentUser?.uid
        val planList = arrayListOf<PlanDto>()

        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                planList.clear()

                for (data in snapshot.children) {
                    val item = data.getValue(PlanDto::class.java)
                    if (item != null) {
                        planList.add(item)
                    }
                }

                planDto.id = planList.size + 1
                planDto.title = title
                planDto.description = description
                planDto.date = "$year/$month/$day"
                planDto.dateTime = getTime()
                planDto.createUid = createUser
                planDto.category = category
                planDto.doneAble = false
                planDto.favorite = false

                database.getReference("plans").child(planDto.id.toString()).setValue(planDto).addOnCompleteListener { task ->
                    result.value = task.isSuccessful
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getTime(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val format = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
        val timeZone = TimeZone.getTimeZone("Asia/Seoul")
        format.timeZone = timeZone
        return format.format(date)
    }
}