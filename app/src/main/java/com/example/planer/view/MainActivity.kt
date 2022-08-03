package com.example.planer.view

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planer.R
import com.example.planer.databinding.ActivityMainBinding
import com.example.planer.databinding.CustomDialogAddScheduleBinding
import com.example.planer.model.PlanDto
import com.example.planer.model.ScheduleDto
import com.example.planer.view.plan.GetPlanActivity
import com.example.planer.view.user.LoginActivity
import com.example.planer.viewmodel.ScheduleViewModel
import com.example.planer.viewmodel.adapter.MainPlanListRecyclerAdapter
import com.example.planer.viewmodel.adapter.TimeTableRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList
import android.view.View as View1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageDrawerOpenBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // header 정보 가져오기
        val headerView = binding.navigationDrawer.getHeaderView(0)
        getUserName(headerView)

        initRecycler()
        initScheduleRecycler()
        navigationClick()

        val loginBtn = headerView.findViewById<AppCompatButton>(R.id.login_button)
        loginBtn.setOnClickListener {
            if (loginBtn.text.toString() == "로그인") startActivity(Intent(this, LoginActivity::class.java))
            else if (loginBtn.text.toString() == "로그아웃") {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        setCalender()
        setMode()
    }

    private fun navigationClick() {
        binding.navigationDrawer.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.create_time_plan -> {
                    dialogLogic()
                }
                R.id.modify_delete_time_plan -> {
                    Toast.makeText(this, "modify_delete_time_plan", Toast.LENGTH_SHORT).show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun dialogLogic() {
        val dialogBinding = CustomDialogAddScheduleBinding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialog = dialogBuilder.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.setTimeModeSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> {  // 시작시간
                        dialogBinding.setScheduleTimePicker.setOnTimeChangedListener { timePicker, hour, minute ->
                            if (hour > 12) {
                                dialogBinding.textStartTime.text = "pm ${hour - 12}: $minute"
                            } else {
                                dialogBinding.textStartTime.text = "am ${hour}: $minute"
                            }
                        }
                    }
                    1 -> {  // 종료 시간
                        dialogBinding.setScheduleTimePicker.setOnTimeChangedListener { timePicker, hour, minute ->
                            if (hour > 12) {
                                dialogBinding.textEndTime.text = "pm ${hour - 12}: $minute"
                            } else {
                                dialogBinding.textEndTime.text = "am ${hour}: $minute"
                            }
                        }
                    }
                    else -> Log.d("ERROR", "onItemSelected: 에러")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        dialogBinding.addScheduleButton.setOnClickListener {
            val startTime = dialogBinding.textStartTime.text.toString()
            val endTime = dialogBinding.textEndTime.text.toString()

            viewModel.createScheduleLogic(startTime, endTime, dialogBinding.scheduleDescriptionEditText.text.toString())
            viewModel.result.observe(this, Observer { result ->
                when(result) {
                    true -> {
                        Toast.makeText(this, "일정을 생성하였습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    false -> {
                        Toast.makeText(this, "일정을 생성하지 못헀습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            })
        }
    }

    private fun initRecycler() {
        val plans :ArrayList<PlanDto> = arrayListOf()
        val adapter = MainPlanListRecyclerAdapter(plans)

        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children) {
                    val item = datasnapshot.getValue(PlanDto::class.java)
                    if (item!!.createUid == auth.currentUser?.uid) {
                        plans.add(item)
                    } else continue
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.allPlansRecyclerView.adapter = adapter
        binding.allPlansRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initScheduleRecycler() {
        val schedules = arrayListOf<ScheduleDto>()
        val adapter = TimeTableRecyclerAdapter(schedules)

        database.getReference("schedules").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(ScheduleDto::class.java)
                    if (item!!.createUid == auth.currentUser?.uid) {
                        schedules.add(item)
                    } else continue
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.scheduleRecyclerView.adapter = adapter
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setMode() {
        binding.timeModeSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View1?, position: Int, id: Long) {
                when(position) {
                    0 -> {
                        binding.calendarView.visibility = View1.VISIBLE
                        binding.scheduleRecyclerView.visibility = View1.GONE
                    }
                    1 -> {
                        binding.calendarView.visibility = View1.GONE
                        binding.scheduleRecyclerView.visibility = View1.VISIBLE
                    }
                    else -> Log.d("ERROR", "onItemSelected: 오류 발생")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun getUserName(headerView: View1) {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(auth.uid.toString()).child("name")

        reference.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (auth.currentUser?.uid != null) {
                    // 데이터 활용
                    headerView.findViewById<TextView>(R.id.text_user_name).text = snapshot.getValue(String::class.java)
                    headerView.findViewById<TextView>(R.id.login_button).text = "로그아웃"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setCalender() {
        binding.calendarView.setOnDateChangeListener { calendarView, year, month, day ->
            startActivity(Intent(this, GetPlanActivity::class.java)
                .putExtra("year", year) // int
                .putExtra("month", month + 1)
                .putExtra("day", day))
        }
    }

}