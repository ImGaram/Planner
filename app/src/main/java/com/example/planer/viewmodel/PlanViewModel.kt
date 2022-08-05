package com.example.planer.viewmodel

import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planer.R
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

    fun setHeaderData(headerView: View) {
        val totalPlanList = arrayListOf<PlanDto>()
        val completedPlanList = arrayListOf<PlanDto>()
        val doingList = arrayListOf<PlanDto>()

        val totalPlan = headerView.findViewById<TextView>(R.id.text_total_plans_count)
        val completedPlan = headerView.findViewById<TextView>(R.id.text_completed_plans_count)
        val doingPlan = headerView.findViewById<TextView>(R.id.text_doing_plan_count)

        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(PlanDto::class.java)
                    if (item!!.createUid == auth.currentUser?.uid) {
                        totalPlanList.add(item)
                        if (item.doneAble == true) completedPlanList.add(item)
                        else doingList.add(item)
                    }
                }

                totalPlan.text = totalPlanList.size.toString()
                completedPlan.text = completedPlanList.size.toString()
                doingPlan.text = doingList.size.toString()
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