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
import com.example.planer.viewmodel.PlanListRecyclerAdapter
import com.example.planer.viewmodel.PlanViewModel

class GetPlanActivity : AppCompatActivity() {
    private val viewModel: PlanViewModel by viewModels()
    private lateinit var binding: ActivityGetPlanBinding

    var temp = "plan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_get_plan)
        binding.getPlan = viewModel

        window.statusBarColor = Color.parseColor("#8021F59D")

        val year = intent.getIntExtra("year", 0)
        val month = intent.getIntExtra("month", 0)
        val day = intent.getIntExtra("day", 0)

        initRecycler(year, month, day)

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
                    dialogBinding.editDescription.text.toString(), year, month, day, temp)
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
                Log.d("Temp", "setCalender: $temp")
                temp = "plan"
            }
        }
    }

    private fun initRecycler(year: Int, month: Int, day: Int) {
        val day = "$year/$month/$day"
        val adapter = PlanListRecyclerAdapter(day)

        binding.planListRecyclerView.adapter = adapter
        binding.planListRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getPlanBtnText() { temp = "plan" }
    private fun getScheduleBtnText() { temp = "schedule" }
    private fun getOtherBtnText() { temp = "other" }

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