package com.example.appque

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    // Declare Firebase Authentication and Database references
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var lottieAnimationView: LottieAnimationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Updated to include Lottie animation

        // Initialize Lottie Animation View
        lottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // Proceed to Firebase user check after animation ends
                checkUserLoginStatus()
            }

            override fun onAnimationCancel(animation: Animator) {
                checkUserLoginStatus()
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })

        // Play Lottie animation
        lottieAnimationView.playAnimation()
    }

    // Check user login status
    private fun checkUserLoginStatus() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.child("users").child(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").value.toString()
                    navigateBasedOnRole(role)
                }
                .addOnFailureListener {
                    navigateToLogin()
                }
        } else {
            navigateToLogin()
        }
    }

    // Navigate based on role
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

    private fun navigateToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
