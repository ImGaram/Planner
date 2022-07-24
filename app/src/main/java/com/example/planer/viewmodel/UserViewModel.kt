package com.example.planer.viewmodel

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserViewModel(application: Application): AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    var result = MutableLiveData<Boolean>()

    fun signInLogic(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = auth.currentUser?.uid.toString()
                val databaseRef = database.reference.child("Users").child(firebaseUser)
                val hashMap: HashMap<String, Any> = HashMap()

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    result.value = false
                }

                hashMap["uid"] = firebaseUser
                hashMap["name"] = name
                hashMap["email"] = email
                databaseRef.setValue(hashMap).addOnCompleteListener { task ->
                    result.value = task.isSuccessful
                }
            }
        }
    }

    fun loginLogic(email: String, password: String) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            result.value = false
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                result.value = it.isSuccessful
            }
        }
    }

    fun updatePasswordLogic(email: String) {
        if (email.isEmpty()) {
            result.value = false
        } else {
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result.value = true
                } else {
                    result.value = false
                    Log.d("VIEWMODEL", "updatePasswordLogic: ${task.exception}")
                }
            }
        }
    }
}