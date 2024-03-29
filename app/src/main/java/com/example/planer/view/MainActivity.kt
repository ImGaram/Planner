package com.example.planer.view

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planer.R
import com.example.planer.databinding.ActivityMainBinding
import com.example.planer.databinding.CustomDialogAddTimeTableBinding
import com.example.planer.model.PlanDto
import com.example.planer.model.TimeTableDto
import com.example.planer.view.plan.DeletePlanActivity
import com.example.planer.view.plan.DeleteTimeTablePlanActivity
import com.example.planer.view.plan.GetPlanActivity
import com.example.planer.view.user.LoginActivity
import com.example.planer.viewmodel.PlanViewModel
import com.example.planer.viewmodel.TimeTableViewModel
import com.example.planer.viewmodel.adapter.MainPlanListRecyclerAdapter
import com.example.planer.viewmodel.adapter.TimeTableRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val viewModel: TimeTableViewModel by viewModels()
    private val planViewModel: PlanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerView = binding.navigationDrawer.getHeaderView(0)

        binding.imageDrawerOpenBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            planViewModel.setHeaderData(headerView)
        }

        setHeaderInfo(headerView)   // header 정보 가져오기
        initRecycler("all")
        initScheduleRecycler()
        navigationClick()
        sendNotification()

        val loginBtn = headerView.findViewById<AppCompatButton>(R.id.login_button)
        loginBtn.setOnClickListener {
            if (loginBtn.text.toString() == "로그인") {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.top_in, R.anim.none)
                finish()
            }
            else if (loginBtn.text.toString() == "로그아웃") {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.top_in, R.anim.none)
                finish()
            }
        }

        setCalender()
        setMode()
        setAllPlansMode()
    }

    private fun setNotification(notCompletePlansCount: Int) {
        val channelId = "my_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, "일정", importance).apply {
                description = "daily schedule"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            val intent = Intent(this, GetPlanActivity::class.java)
                .putExtra("year", planViewModel.getDate().substring(0 until 4))
                .putExtra("month", planViewModel.getDate().substring(5 until 6))
                .putExtra("day", planViewModel.getDate().substring(7 until 9))
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_baseline_calendar_month_24) // small icon
                .setContentTitle("남은 할일")   // 타이틀
                .setContentText("아직 완료하지 못한 일이 ${notCompletePlansCount}개 남았어요!")    // 내용
                .setAutoCancel(true)    // 알림 클릭시 알림 제거 여부
                .setContentIntent(pendingIntent)    // 알림 클릭시 pendingIntent 의 activity 로 이동

            val notificationBuilderId = 100
            NotificationManagerCompat.from(this).apply {
                notify(notificationBuilderId, notificationBuilder.build())
            }
        }
    }

    private fun navigationClick() {
        binding.navigationDrawer.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.create_time_schedule -> {
                    if (auth.uid == null) {
                        Toast.makeText(this, "시간표 생성은 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
                    } else dialogLogic()
                }
                R.id.modify_delete_time_schedule -> {
                    if (auth.uid == null) {
                        Toast.makeText(this, "일정 삭제는 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
                    } else startActivity(Intent(this, DeletePlanActivity::class.java))
                }
                R.id.delete_time_table_plan -> {
                    if (auth.uid == null) {
                        Toast.makeText(this, "시간표 삭제는 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
                    } else startActivity(Intent(this, DeleteTimeTablePlanActivity::class.java))
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun dialogLogic() {
        val dialogBinding = CustomDialogAddTimeTableBinding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialog = dialogBuilder.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.setTimeModeSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> {  // 시작시간
                        dialogBinding.setScheduleTimePicker.setOnTimeChangedListener { _, hour, minute ->
                            if (hour > 12) {
                                if (minute < 10) {
                                    dialogBinding.textStartTime.text = "pm ${hour - 12}: 0${minute}"
                                } else dialogBinding.textStartTime.text = "pm ${hour - 12}: $minute"
                            } else {
                                if (minute < 10) {
                                    dialogBinding.textStartTime.text = "am ${hour - 12}: 0${minute}"
                                } else dialogBinding.textStartTime.text = "am ${hour}: $minute"
                            }
                        }
                    }
                    1 -> {  // 종료 시간
                        dialogBinding.setScheduleTimePicker.setOnTimeChangedListener { _, hour, minute ->
                            if (hour > 12) {
                                if (minute < 10) {
                                    dialogBinding.textEndTime.text = "pm ${hour - 12}: 0${minute}"
                                } else dialogBinding.textEndTime.text = "pm ${hour - 12}: $minute"
                            } else {
                                if (minute < 10) {
                                    dialogBinding.textEndTime.text = "am ${hour - 12}: 0${minute}"
                                } else dialogBinding.textEndTime.text = "am ${hour}: $minute"
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
                        initScheduleRecycler()  // 좋은 방법 찾기
                    }
                    false -> {
                        Toast.makeText(this, "일정을 생성하지 못헀습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            })
        }
    }

    private fun setAllPlansMode() {
        binding.categoryModeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> initRecycler("all")
                    1 -> { initRecycler("plan") }
                    2 -> { initRecycler("schedule") }
                    3 -> { initRecycler("other") }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun initRecycler(category: String) {
        val plans :ArrayList<PlanDto> = arrayListOf()
        val adapter = MainPlanListRecyclerAdapter(plans)

        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {     // todo
                for (datasnapshot in snapshot.children) {
                    val item = datasnapshot.getValue(PlanDto::class.java)
                    Log.d("category", "onDataChange: $category")
                    if (category == "all") {
                        if (item!!.createUid == auth.currentUser?.uid) {
                            plans.add(item)
                        } else continue
                    } else {
                        if (item!!.createUid == auth.currentUser?.uid && item.category == category) {
                            plans.add(item)
                        } else continue
                    }
                }
                if (plans.size == 0) {
                    binding.allPlansRecyclerView.visibility = View.GONE
                    binding.allPlansEmptyText.visibility = View.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.allPlansRecyclerView.adapter = adapter
        binding.allPlansRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initScheduleRecycler() {
        val schedules = arrayListOf<TimeTableDto>()
        val adapter = TimeTableRecyclerAdapter(schedules)

        database.getReference("schedules").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(TimeTableDto::class.java)
                    if (item!!.createUid == auth.currentUser?.uid && item.uploadDate == viewModel.setToday()) {
                        schedules.add(item)
                    } else continue
                }
                if (schedules.size == 0) {
                    binding.scheduleRecyclerView.visibility = View.GONE
                    binding.scheduleEmptyText.visibility = View.VISIBLE
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
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> {
                        binding.calendarView.visibility = View.VISIBLE
                        binding.scheduleRecyclerView.visibility = View.GONE
                    }
                    1 -> {
                        binding.calendarView.visibility = View.GONE
                        binding.scheduleRecyclerView.visibility = View.VISIBLE
                    }
                    else -> Log.d("ERROR", "onItemSelected: 오류 발생")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setHeaderInfo(headerView: View) {
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

    private fun sendNotification() {
        var notCompletePlansCount = 0
        database.getReference("plans").addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(PlanDto::class.java)
                    if (item?.doneAble == false && item.date == planViewModel.getDate() && item.createUid == auth.currentUser?.uid) {
                        notCompletePlansCount += 1
                    } else continue
                }

                if (notCompletePlansCount != 0) {
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

                    val intent = Intent(this@MainActivity, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(this@MainActivity, AlarmReceiver.NOTIFICATION_ID,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    if (getTodayHour() == "23") {
                        val repeatInterval = AlarmManager.INTERVAL_DAY
                        val triggerTime = (SystemClock.elapsedRealtime() + repeatInterval)
                        alarmManager.setInexactRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime, repeatInterval,
                            pendingIntent
                        )
                        "Alarm On"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setCalender() {
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            if (auth.uid == null) {
                Toast.makeText(this, "일정 작성은 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, GetPlanActivity::class.java)
                    .putExtra("year", year) // int
                    .putExtra("month", month + 1)
                    .putExtra("day", day))
                overridePendingTransition(R.anim.top_in, R.anim.none)
            }

        }
    }

    private fun getTodayHour(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val format = SimpleDateFormat("HH", Locale.KOREA)
        val timeZone = TimeZone.getTimeZone("Asia/Seoul")
        format.timeZone = timeZone
        return format.format(date)
    }
}