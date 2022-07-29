package com.example.planer.view.plan

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planer.R
import com.example.planer.databinding.ActivityGetPlanBinding
import com.example.planer.databinding.CustomDialogAddPlanBinding
import com.example.planer.model.PlanDto
import com.example.planer.viewmodel.PlanViewModel
import com.example.planer.viewmodel.adapter.FavoriteListRecyclerAdapter
import com.example.planer.viewmodel.adapter.PlanListRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GetPlanActivity : AppCompatActivity() {
    private val viewModel: PlanViewModel by viewModels()
    private lateinit var binding: ActivityGetPlanBinding

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    var category = "plan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_get_plan)
        binding.getPlan = viewModel

        window.statusBarColor = Color.parseColor("#8021F59D")

        val year = intent.getIntExtra("year", 0)
        val month = intent.getIntExtra("month", 0)
        val day = intent.getIntExtra("day", 0)

        initRecycler(year, month, day)
        initFavoriteRecycler(year, month, day)

        binding.addPlanButton.setOnClickListener {
            val dialogBinding = CustomDialogAddPlanBinding.inflate(LayoutInflater.from(this))
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
            dialogBinding.dialogTitleText.text = "${year}년 ${month}월 ${day}일 일정 추가"
            val dialog = dialogBuilder.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            categoryBtnEvent(dialogBinding)

            dialogBinding.createPlanBtn.setOnClickListener {
                viewModel.addPlanLogic(dialogBinding.editPlanTitle.text.toString(),
                    dialogBinding.editDescription.text.toString(), year, month, day,
                    category)
                viewModel.result.observe(this, Observer {
                    when (it) {
                        true -> {
                            Toast.makeText(this, "일정을 생성하였습니다.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        false -> {
                            Toast.makeText(this, "일정 생성을 실패했습니다.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
                })

                dialog.dismiss()
                Log.d("Temp", "setCalender: $category")
                category = "plan"
            }
        }
    }

    private fun initRecycler(year: Int, month: Int, day: Int) {
        val planList :ArrayList<PlanDto> = arrayListOf()
        val planUidList = arrayListOf<String>()

        val date = "$year/$month/$day"
        val adapter = PlanListRecyclerAdapter(planList, planUidList, this, date)

        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children) {
                    val item = datasnapshot.getValue(PlanDto::class.java)
                    if (item?.date == date && item.createUid == auth.currentUser?.uid) {
                        planList.add(item)
                        planUidList.add(item.id.toString())
                    } else continue
                }
                adapter.notifyDataSetChanged()
                Log.d("List", "onDataChange: $planList")

                if (planList.size == 0) {
                    binding.planListRecyclerView.visibility = View.GONE
                    binding.noticeNoRecyclerItemText.visibility = View.VISIBLE
                    binding.countNotCompletedText.text = "아직 일정을 작성하지 않으셨습니다."
                } else {
                    getNotCompletedPlan(date)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })


        binding.planListRecyclerView.adapter = adapter
        binding.planListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initFavoriteRecycler(year: Int, month: Int, day: Int) {
        val favoriteList = arrayListOf<PlanDto>()
        val planNumberList = arrayListOf<String>()

        val date = "$year/$month/$day"
        val adapter = FavoriteListRecyclerAdapter(favoriteList, planNumberList, this, date)

        getFavoriteList(favoriteList, planNumberList, adapter, date)

        binding.favoriteRecyclerView.adapter = adapter
        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    fun getFavoriteList(
        favoriteList: ArrayList<PlanDto>,
        planNumberList: ArrayList<String>,
        adapter: FavoriteListRecyclerAdapter,
        date: String
    ) {
        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteList.clear()

                for (snap in snapshot.children) {
                    val item = snap.getValue(PlanDto::class.java)
                    if (item?.favorite == true && item.date == date && item.createUid == auth.currentUser?.uid) {
                        favoriteList.add(item)
                        planNumberList.add(item.id.toString())
                    } else continue
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR", "onCancelled: ${error.details}", error.toException())
            }
        })
    }

    fun getNotCompletedPlan(day: String) {
        val notCompletedList: ArrayList<PlanDto> = arrayListOf()

        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notCompletedList.clear()

                for (snap in snapshot.children) {
                    val item = snap.getValue(PlanDto::class.java)
                    if (item?.doneAble == false && item.date == day && item.createUid == auth.currentUser?.uid) {
                        notCompletedList.add(item)
                    } else continue
                }

                if (notCompletedList.size == 0) {
                    binding.countNotCompletedText.text = "오늘의 일정을 모두 완료하였습니다!"
                } else {
                    binding.countNotCompletedText.text = "아직 완료하지 못한 일이 ${notCompletedList.size}개 있습니다."
                }
            }

            override fun onCancelled(error: DatabaseError) { Log.e("ERROR", "onCancelled: ${error.details}", error.toException()) }
        })
    }

    private fun getPlanBtnText() { category = "plan" }
    private fun getScheduleBtnText() { category = "schedule" }
    private fun getOtherBtnText() { category = "other" }

    private fun categoryBtnEvent(dialogBinding: CustomDialogAddPlanBinding) {
        val planBtn = dialogBinding.selectCategoryPlanBtn
        val scheduleBtn = dialogBinding.setCategoryScheduleBtn
        val otherBtn = dialogBinding.selectCategoryOtherBtn

        val onClickListener = View.OnClickListener {
            when(it.id) {
                R.id.select_category_plan_btn -> {
                    getPlanBtnText()

                    planBtn.setTextColor(Color.WHITE)
                    planBtn.setBackgroundResource(R.drawable.button_bg1)

                    scheduleBtn.setTextColor(Color.BLACK)
                    scheduleBtn.setBackgroundResource(R.drawable.button_bg2)
                    otherBtn.setTextColor(Color.BLACK)
                    otherBtn.setBackgroundResource(R.drawable.button_bg2)
                }
                R.id.set_category_schedule_btn -> {
                    getScheduleBtnText()

                    scheduleBtn.setTextColor(Color.WHITE)
                    scheduleBtn.setBackgroundResource(R.drawable.button_bg1)

                    planBtn.setTextColor(Color.BLACK)
                    planBtn.setBackgroundResource(R.drawable.button_bg2)
                    otherBtn.setTextColor(Color.BLACK)
                    otherBtn.setBackgroundResource(R.drawable.button_bg2)
                }
                R.id.select_category_other_btn -> {
                    getOtherBtnText()

                    otherBtn.setTextColor(Color.WHITE)
                    otherBtn.setBackgroundResource(R.drawable.button_bg1)

                    scheduleBtn.setTextColor(Color.BLACK)
                    scheduleBtn.setBackgroundResource(R.drawable.button_bg2)
                    planBtn.setTextColor(Color.BLACK)
                    planBtn.setBackgroundResource(R.drawable.button_bg2)
                }
            }
        }
        planBtn.setOnClickListener(onClickListener)
        scheduleBtn.setOnClickListener(onClickListener)
        otherBtn.setOnClickListener(onClickListener)
    }
}