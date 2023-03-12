package com.example.planer.view.plan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planer.R
import com.example.planer.databinding.ActivityDeleteTimeTablePlanBinding
import com.example.planer.model.TimeTableDto
import com.example.planer.viewmodel.TimeTableViewModel
import com.example.planer.viewmodel.adapter.DeleteTimeTableRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteTimeTablePlanActivity : AppCompatActivity() {
    private val viewModel by viewModels<TimeTableViewModel>()
    private lateinit var binding: ActivityDeleteTimeTablePlanBinding
    private val database = FirebaseDatabase.getInstance()
    private lateinit var adapter: DeleteTimeTableRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_delete_time_table_plan)
        binding.deleteTimeTable = viewModel

        initRecycler()

        binding.backMainButton2.setOnClickListener { finish() }
        binding.deleteTimeTablePlans.setOnClickListener {
            deleteTimeTableLogic()
        }
    }

    private fun initRecycler() {
        val deleteTimeTableList = arrayListOf<TimeTableDto>()
        val numList = arrayListOf<String>()
        adapter = DeleteTimeTableRecyclerAdapter(deleteTimeTableList, numList)

        database.getReference("schedules").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                deleteTimeTableList.clear()
                numList.clear()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(TimeTableDto::class.java)

                    deleteTimeTableList.add(item!!)
                    numList.add(item.id.toString())
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.deleteTimeTablePlanRecyclerView.adapter = adapter
        binding.deleteTimeTablePlanRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun deleteTimeTableLogic() {
        database.getReference("schedules").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(TimeTableDto::class.java)
                    if (item?.deleteAble == true) {
                        database.getReference("schedules").child(item.id.toString()).removeValue().addOnCompleteListener {
                            Toast.makeText(this@DeleteTimeTablePlanActivity, "일정을 삭제했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else continue
                }
                initRecycler()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}