package com.example.appque

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class StudentWindow8Activity : AppCompatActivity() {

    private var userName: String? = null
    private var userIdNumber: String? = null
    private var userCourse: String? = null
    private var userYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_window8)

        userName = intent.getStringExtra("name")
        userIdNumber = intent.getStringExtra("idNumber")
        userCourse = intent.getStringExtra("course")
        userYear = intent.getStringExtra("year")

        Log.d(
            "StudentWindow8Activity",
            "Received Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )

        findViewById<ImageButton>(R.id.backArrowButton).setOnClickListener {
            finish()
        }

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
            putExtra("name", userName)
            putExtra("idNumber", userIdNumber)
            putExtra("course", userCourse)
            putExtra("year", userYear)
        }
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                // Clear user session (if needed)
                FirebaseAuth.getInstance().signOut()

                // Navigate to login activity and clear the back stack
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish() // Close the current activity
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

}

