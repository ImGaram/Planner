package com.example.planer.view.user

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.planer.R
import com.example.planer.databinding.ActivityLoginBinding
import com.example.planer.view.MainActivity
import com.example.planer.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: UserViewModel by viewModels()

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.login = viewModel

        window.statusBarColor = Color.parseColor("#8021F59D")

        binding.createNewUserButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.mainLoginButton.setOnClickListener {
            val email = binding.editEmailText.text.toString()
            val password = binding.editPasswordText.text.toString()

            binding.loginProgressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            viewModel.loginLogic(email, password)
            viewModel.result.observe(this, Observer {
                if (it == true) {
                    binding.loginProgressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    val intent = Intent(this, MainActivity::class.java).putExtra("uid", auth.uid)
                    startActivity(intent)
                }
                else {
                    binding.loginProgressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    Toast.makeText(this, "로그인 실패했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            })
        }
        binding.guestButton.setOnClickListener { finish() }
    }
}