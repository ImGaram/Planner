package com.example.planer.viewmodel

import android.app.Application
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserViewModel(application: Application): AndroidViewModel(application) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
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
                hashMap["password"] = password
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
}