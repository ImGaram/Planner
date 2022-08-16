package com.example.planer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planer.model.ScheduleDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ScheduleViewModel: ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    var result = MutableLiveData<Boolean>()
    var scheduleDto = ScheduleDto()

    fun createScheduleLogic(startTime: String, endTime: String, description: String) {
        val scheduleList = arrayListOf<ScheduleDto>()

        database.getReference("schedules").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val item = data.getValue(ScheduleDto::class.java)
                    if (item != null) {
                        scheduleList.add(item)
                    }
                }

                scheduleDto.id = scheduleList.last().id!! + 1
                scheduleDto.createUid = auth.currentUser?.uid
                scheduleDto.startTime = startTime
                scheduleDto.uploadDate = setToday()
                scheduleDto.endTime = endTime
                scheduleDto.description = description

                database.getReference("schedules").child(scheduleDto.id.toString()).setValue(scheduleDto).addOnCompleteListener { task ->
                    result.value = task.isSuccessful
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setToday(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val format = SimpleDateFormat("E", Locale.KOREA)

        return format.format(date)
    }
}