package com.example.planer.view.user

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.planer.R
import com.example.planer.databinding.ActivityLoginBinding
import com.example.planer.databinding.CustomDialogFindPasswordBinding
import com.example.planer.view.MainActivity
import com.example.planer.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: UserViewModel by viewModels()

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
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else {
                    binding.loginProgressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    Toast.makeText(this, "로그인 실패했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            })
        }
        binding.guestButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.forgetPasswordText.setOnClickListener {
            val dialogBinding = CustomDialogFindPasswordBinding.inflate(LayoutInflater.from(this))
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
            val dialog = dialogBuilder.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialogBinding.updatePasswordButton.setOnClickListener {
                dialogBinding.changePasswordProgressBar.visibility = View.VISIBLE
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                setNewPassword(dialogBinding.getEmailText.text.toString(), dialogBinding, dialog)
            }
            dialogBinding.updatePasswordCancelButton.setOnClickListener { dialog.dismiss() }
        }
    }

    private fun setNewPassword(email: String, dialogBinding: CustomDialogFindPasswordBinding, dialog: AlertDialog) {
        viewModel.updatePasswordLogic(email)
        viewModel.result.observe(this, Observer {
            if (it == true) {
                dialogBinding.changePasswordProgressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                Toast.makeText(this, "비밀번호 변경 메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            else {
                dialogBinding.changePasswordProgressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                Toast.makeText(this, "비밀번호 변경 메일을 보내지 못했습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }
}