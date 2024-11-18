package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
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
        Log.d(
            "WindowSelectionActivity",
            "Intent Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )

        // Initialize buttons and set up click listeners for each one
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.buttonCashier).setOnClickListener {
            navigateToWithUserData(StudentCashierActivity::class.java)
        }

        findViewById<Button>(R.id.buttonWindow1).setOnClickListener {
            handleWindowClick("Window 1", StudentWindow1Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow2).setOnClickListener {
            handleWindowClick("Window 2", StudentWindow2Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow3).setOnClickListener {
            handleWindowClick("Window 3", StudentWindow3Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow4).setOnClickListener {
            handleWindowClick("Window 4", StudentWindow4Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow5).setOnClickListener {
            handleWindowClick("Window 5", StudentWindow5Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow6).setOnClickListener {
            handleWindowClick("Window 6", StudentWindow6Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow7).setOnClickListener {
            handleWindowClick("Window 7", StudentWindow7Activity::class.java)
        }
        findViewById<Button>(R.id.buttonWindow8).setOnClickListener {
            handleWindowClick("Window 8", StudentWindow8Activity::class.java)
        }
    }

    private fun <T> handleWindowClick(windowName: String, destination: Class<T>) {
        Log.d("WindowSelectionActivity", "Checking access for $windowName")

        // Check if userCourse or userYear are null, empty, or placeholders
        if (userCourse.isNullOrEmpty() || userYear.isNullOrEmpty() ||
            userCourse == "Select Course" || userYear == "Select Year") {
            Toast.makeText(
                this,
                "Cannot access $windowName. User data is incomplete or invalid.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e(
                "WindowSelectionActivity",
                "Access to $windowName denied: Missing or invalid user data -> Course: $userCourse, Year: $userYear"
            )
        } else {
            Log.d("WindowSelectionActivity", "Access granted for $windowName -> Navigating.")
            navigateToWithUserData(destination)
        }
    }

    // Function to handle navigation with user data
    private fun <T> navigateToWithUserData(destination: Class<T>) {
        val intent = Intent(this, destination).apply {
            putExtra("name", userName ?: "Default Name")
            putExtra("idNumber", userIdNumber ?: "Default ID")
            putExtra("course", userCourse ?: "Default Course")
            putExtra("year", userYear ?: "Default Year")
        }
        Log.d("WindowSelectionActivity", "Navigating to $destination with user data.")
        startActivity(intent)
    }

    // Override the back button behavior to exit the app
    override fun onBackPressed() {
        finishAffinity() // Closes all activities and exits to the home screen
    }
}
