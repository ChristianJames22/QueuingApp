package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityWindowSelectionBinding

class WindowSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWindowSelectionBinding
    private var userName: String? = null
    private var userIdNumber: String? = null
    private var userCourse: String? = null
    private var userYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityWindowSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve user data passed from MainActivity
        userName = intent.getStringExtra("name")
        userIdNumber = intent.getStringExtra("id")
        userCourse = intent.getStringExtra("course")
        userYear = intent.getStringExtra("year")

        // Debugging: Log the received user data
        Log.d(
            "WindowSelectionActivity",
            "Intent Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )

        // Set up click listeners for buttons using View Binding
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.buttonCashier.setOnClickListener {
            navigateToWithUserData(StudentCashierActivity::class.java)
        }

        binding.buttonWindow1.setOnClickListener {
            handleWindowClick("Window 1", StudentWindow1Activity::class.java)
        }
        binding.buttonWindow2.setOnClickListener {
            handleWindowClick("Window 2", StudentWindow2Activity::class.java)
        }
        binding.buttonWindow3.setOnClickListener {
            handleWindowClick("Window 3", StudentWindow3Activity::class.java)
        }
        binding.buttonWindow4.setOnClickListener {
            handleWindowClick("Window 4", StudentWindow4Activity::class.java)
        }
        binding.buttonWindow5.setOnClickListener {
            handleWindowClick("Window 5", StudentWindow5Activity::class.java)
        }
        binding.buttonWindow6.setOnClickListener {
            handleWindowClick("Window 6", StudentWindow6Activity::class.java)
        }
        binding.buttonWindow7.setOnClickListener {
            handleWindowClick("Window 7", StudentWindow7Activity::class.java)
        }
        binding.buttonWindow8.setOnClickListener {
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

    private fun <T> navigateToWithUserData(destination: Class<T>) {
        val intent = Intent(this, destination).apply {
            putExtra("name", userName ?: "Default Name")
            putExtra("id", userIdNumber ?: "Default ID")
            putExtra("course", userCourse ?: "Default Course")
            putExtra("year", userYear ?: "Default Year")
        }
        Log.d("WindowSelectionActivity", "Navigating to $destination with user data.")
        startActivity(intent)
    }

    override fun onBackPressed() {
        finishAffinity() // Closes all activities and exits to the home screen
    }
}
