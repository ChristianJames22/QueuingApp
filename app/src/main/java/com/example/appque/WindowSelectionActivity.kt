package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WindowSelectionActivity : AppCompatActivity() {

    private var userName: String? = null
    private var userIdNumber: String? = null
    private var userCourse: String? = null
    private var userYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_window_selection)

        // Retrieve user data passed from MainActivity
        userName = intent.getStringExtra("name")
        userIdNumber = intent.getStringExtra("idNumber")
        userCourse = intent.getStringExtra("course")
        userYear = intent.getStringExtra("year")

        // Debugging: Log the received user data
        Log.d("WindowSelectionActivity", "Intent Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear")

        // Initialize buttons and set up click listeners for each one
        findViewById<Button>(R.id.buttonCashier).setOnClickListener {
            navigateToWithUserData(StudentCashierActivity::class.java) // Pass user data to StudentCashierActivity
        }

        findViewById<Button>(R.id.buttonWindow1).setOnClickListener {
            navigateToWithUserData(StudentWindow1Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow2).setOnClickListener {
            navigateToWithUserData(StudentWindow2Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow3).setOnClickListener {
            navigateToWithUserData(StudentWindow3Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow4).setOnClickListener {
            navigateToWithUserData(StudentWindow4Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow5).setOnClickListener {
            navigateToWithUserData(StudentWindow5Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow6).setOnClickListener {
            navigateToWithUserData(StudentWindow6Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow7).setOnClickListener {
            navigateToWithUserData(StudentWindow7Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow8).setOnClickListener {
            navigateToWithUserData(StudentWindow8Activity::class.java)
        }
    }

    // Function to handle navigation with user data
    private fun <T> navigateToWithUserData(destination: Class<T>) {
        val intent = Intent(this, destination).apply {
            putExtra("name", userName)
            putExtra("idNumber", userIdNumber)
            putExtra("course", userCourse)
            putExtra("year", userYear)
        }
        startActivity(intent)
    }

    // Override the back button behavior to exit the app
    override fun onBackPressed() {
        finishAffinity() // Closes all activities and exits to the home screen
    }
}
