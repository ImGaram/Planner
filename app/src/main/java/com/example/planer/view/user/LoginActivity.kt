package com.example.planer.view.user

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.planer.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#8021F59D")

        binding.createNewUserButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.guestButton.setOnClickListener { finish() }
    }
}