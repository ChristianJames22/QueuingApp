package com.example.appque

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow7Binding
import com.google.firebase.auth.FirebaseAuth

class StudentWindow7Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow7Binding
    private var userName: String? = null
    private var userIdNumber: String? = null
    private var userCourse: String? = null
    private var userYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentWindow7Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve user data passed from WindowSelectionActivity
        userName = intent.getStringExtra("name")
        userIdNumber = intent.getStringExtra("id")
        userCourse = intent.getStringExtra("course")
        userYear = intent.getStringExtra("year")

        Log.d(
            "StudentCashierActivity",
            "Received Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )

        // Back arrow button functionality
        binding.backArrowButton.setOnClickListener {
            finish()
        }

        // Settings button functionality
        binding.settingsButton.setOnClickListener {
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
            putExtra("id", userIdNumber ?: "N/A")
            putExtra("course", userCourse ?: "N/A")
            putExtra("year", userYear ?: "N/A")
        }
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
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
        builder.create().show()
    }
}
