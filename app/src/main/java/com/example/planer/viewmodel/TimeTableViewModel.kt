package com.example.planer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planer.model.TimeTableDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class TimeTableViewModel: ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    var result = MutableLiveData<Boolean>()
    var timeTableDto = TimeTableDto()

    fun createScheduleLogic(startTime: String, endTime: String, description: String) {
        val scheduleList = arrayListOf<TimeTableDto>()

        database.getReference("schedules").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val item = data.getValue(TimeTableDto::class.java)
                    if (item != null) {
                        scheduleList.add(item)
                    }
                }

                timeTableDto.id = scheduleList.last().id!! + 1
                timeTableDto.createUid = auth.currentUser?.uid
                timeTableDto.startTime = startTime
                timeTableDto.uploadDate = setToday()
                timeTableDto.endTime = endTime
                timeTableDto.description = description
                timeTableDto.deleteAble = false

                database.getReference("schedules").child(timeTableDto.id.toString()).setValue(timeTableDto).addOnCompleteListener { task ->
                    result.value = task.isSuccessful
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setToday(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val format = SimpleDateFormat("E", Locale.KOREA)    // 요일만 빼오기

        return format.format(date)
    }
}