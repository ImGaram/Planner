package com.example.planer.view.user

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.planer.R
import com.example.planer.databinding.ActivitySignInBinding
import com.example.planer.viewmodel.SignInViewModel

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        binding.signIn = viewModel

        window.statusBarColor = Color.parseColor("#8021F59D")

        binding.createUserButton.setOnClickListener {
            val name = binding.editNewUserNameText.text.toString()
            val email = binding.editNewEmailText.text.toString()
            val password = binding.editNewPasswordText.text.toString()

            // progress bar 설정
            binding.signInProgress.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)   // progress bar 진행 중일때 클릭 막기

            viewModel.signInLogic(name, email, password)
            viewModel.result.observe(this, Observer {
                if (it == true) {
                    Toast.makeText(this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    binding.signInProgress.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)    // 클릭 풀기
                    finish()
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                }
                else Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
            })
        }
        binding.createUserCancelBtn.setOnClickListener { finish() }
    }
}