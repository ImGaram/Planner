package com.example.planer.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import com.example.planer.R
import com.example.planer.databinding.ActivityMainBinding
import com.example.planer.view.plan.GetPlanActivity
import com.example.planer.view.user.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()

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
    }

    private fun getUserName(headerView: View) {
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