package com.example.planer.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import com.example.planer.R
import com.example.planer.databinding.ActivityMainBinding
import com.example.planer.databinding.CustomDialogSetPlanBinding
import com.example.planer.view.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var temp = "plan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageDrawerOpenBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // header 정보 가져오기
        val headerView = binding.navigationDrawer.getHeaderView(0)
        headerView.findViewById<AppCompatButton>(R.id.login_button).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        setCalender()
    }

    private fun getPlanBtnText() {
        temp = "plan"
    }

    private fun getScheduleBtnText() {
        temp = "schedule"
    }

    private fun getOtherBtnText() {
        temp = "other"
    }

    private fun setCalender() {
        binding.calendarView.setOnDateChangeListener { calendarView, year, month, day ->
            val dialogBinding = CustomDialogSetPlanBinding.inflate(LayoutInflater.from(this))
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
            dialogBinding.dialogTitleText.text = "${year}년 ${month+1}월 ${day}일 일정 추가"
            val dialog = dialogBuilder.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            categoryBtnEvent(dialogBinding)

            dialogBinding.createPlanBtn.setOnClickListener {
                dialog.dismiss()
                Log.d("Temp", "setCalender: $temp")
                temp = "plan"
            }
        }
    }

    private fun categoryBtnEvent(dialogBinding: CustomDialogSetPlanBinding) {
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