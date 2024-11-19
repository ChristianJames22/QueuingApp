package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityWindow6Binding
import com.google.firebase.auth.FirebaseAuth

class Window6Activity : AppCompatActivity() {

    private lateinit var binding: ActivityWindow6Binding
    private var userName: String? = null
    private var userIdNumber: String? = null
    private var userCourse: String? = null
    private var userYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWindow6Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data passed through Intent
        userName = intent.getStringExtra("name")
        userIdNumber = intent.getStringExtra("id")  // Retrieve ID
        userCourse = intent.getStringExtra("course")
        userYear = intent.getStringExtra("year")

        Log.d(
            "Window1Activity",
            "Received Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )

        // Display user data in UI
        findViewById<TextView>(R.id.textName)?.text = "Name: ${userName ?: "Unknown"}"
        findViewById<TextView>(R.id.textIdNumber)?.text = "ID No.: ${userIdNumber ?: "N/A"}"
        findViewById<TextView>(R.id.textCourse)?.text = "Course: ${userCourse ?: "N/A"}"
        findViewById<TextView>(R.id.textYear)?.text = "Year: ${userYear ?: "N/A"}"

        // Settings button functionality
        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            showSettingsMenu()
        }
    }

    private fun showSettingsMenu() {
        val options = arrayOf("Profile", "Logout")
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToProfileActivity()
                    1 -> showLogoutConfirmationDialog()
                }
            }
            .show()
    }

    private fun navigateToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra("name", userName ?: "N/A")
            putExtra("id", userIdNumber ?: "N/A")  // Pass the ID
            putExtra("course", userCourse ?: "N/A")
            putExtra("year", userYear ?: "N/A")
        }
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finishAffinity()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
