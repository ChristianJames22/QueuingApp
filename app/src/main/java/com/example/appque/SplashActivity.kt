package com.example.appque

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {

    // Declare Firebase Authentication and Database references
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth and FirebaseDatabase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Check if a user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Fetch user role from Firebase Realtime Database
            database.child("users").child(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    // Retrieve user role and navigate accordingly
                    val role = snapshot.child("role").value.toString()
                    navigateBasedOnRole(role)
                }
                .addOnFailureListener {
                    // If fetching role fails, navigate to login screen
                    navigateToLogin()
                }
        } else {
            // If no user is logged in, navigate to login screen
            navigateToLogin()
        }
    }

    // Navigate to the appropriate activity based on the user's role
    private fun navigateBasedOnRole(role: String) {
        val destination = when (role) {
            "admin" -> AdminActivity::class.java
            "window1" -> CashierActivity::class.java
            "window2" -> Window2Activity::class.java
            "window3" -> Window3Activity::class.java
            "window4" -> Window4Activity::class.java
            "window5" -> Window5Activity::class.java
            "window6" -> Window6Activity::class.java
            "window7" -> Window7Activity::class.java
            "window8" -> Window8Activity::class.java
            "student" -> StudentActivity::class.java
            else -> MainActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
    }


    // Navigate to the login screen
    private fun navigateToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
