package com.example.planer.view.plan

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planer.R
import com.example.planer.databinding.ActivityDeletePlanBinding
import com.example.planer.model.PlanDto
import com.example.planer.viewmodel.PlanViewModel
import com.example.planer.viewmodel.adapter.DeletePlanRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeletePlanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeletePlanBinding
    private val auth = FirebaseAuth.getInstance()
    private val dataBase = FirebaseDatabase.getInstance()
    private val viewModel by viewModels<PlanViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_delete_plan)
        binding.deletePlan = viewModel

        initRecycler()

        binding.backArrowToMainImage.setOnClickListener { finish() }
        binding.deletePlanImage.setOnClickListener { deletePlanLogic() }
    }

    private fun initRecycler() {
        val deleteList = arrayListOf<PlanDto>()
        val deleteNumList = arrayListOf<String>()
        val adapter = DeletePlanRecyclerAdapter(deleteList, deleteNumList)

        dataBase.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                deleteList.clear()
                deleteNumList.clear()

                for (documentSnapshot in snapshot.children) {
                    val item = documentSnapshot.getValue(PlanDto::class.java)
                    if (auth.currentUser!!.uid == item?.createUid) {
                        deleteList.add(item)
                        deleteNumList.add(item.id.toString())
                    } else continue
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.deleteListRecyclerView.adapter = adapter
        binding.deleteListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun deletePlanLogic() {
        dataBase.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapShot in snapshot.children) {
                    val item = dataSnapShot.getValue(PlanDto::class.java)
                    if (item?.deleteAble == true) {
                        dataBase.getReference("plans").child(item.id.toString()).removeValue()
                    } else continue
                }
                initRecycler()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}