package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.fragments.WindowsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Fetch user role and navigate accordingly
            fetchUserRole(currentUser.uid)
        } else {
            // Redirect to login screen
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserRole(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "student"
                    val name = document.getString("name") ?: "User"

                    Log.d("SplashActivity", "User role fetched: $role")
                    navigateBasedOnRole(role, name)
                } else {
                    Log.e("SplashActivity", "No user document found for UID: $uid")
                    redirectToLogin()
                }
            }
            .addOnFailureListener { e ->
                Log.e("SplashActivity", "Error fetching user role: ${e.message}")
                redirectToLogin()
            }
    }

    private fun navigateBasedOnRole(role: String, name: String) {
        val destinationClass = when (role) {
            "admin" -> AdminActivity::class.java
            "staff" -> CashierActivity::class.java
            "staff2" -> Window2Activity::class.java
            "staff3" -> Window3Activity::class.java
            "staff4" -> Window4Activity::class.java
            "staff5" -> Window5Activity::class.java
            "staff6" -> Window6Activity::class.java
            "staff7" -> Window7Activity::class.java
            "staff8" -> Window8Activity::class.java

            "student" -> StudentActivity::class.java

            else -> MainActivity::class.java // Fallback to login screen
        }

        val intent = Intent(this, destinationClass).apply {
            putExtra("name", name)
        }
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}