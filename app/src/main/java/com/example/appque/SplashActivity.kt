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
            "staff1" -> CashierActivity::class.java
            "staff2" -> Window2Activity::class.java
            "staff3" -> Window3Activity::class.java
            "staff4" -> Window4Activity::class.java
            "staff5" -> Window5Activity::class.java
            "staff6" -> Window6Activity::class.java
            "staff7" -> Window7Activity::class.java
            "staff8" -> Window8Activity::class.java
            "student" -> StudentActivity::class.java
            else -> MainActivity::class.java // Default to MainActivity for unknown roles
        }

        // Start the determined activity and finish the splash screen
        startActivity(Intent(this, destination))
        finish()
    }

    // Navigate to the login screen
    private fun navigateToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
