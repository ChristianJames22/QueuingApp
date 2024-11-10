package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class StudentCashierActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_cashier)  // Use the Cashier layout

        // Handle the Back Arrow Button logic
        val backArrowButton = findViewById<ImageButton>(R.id.backArrowButton)
        backArrowButton.setOnClickListener {
            navigateBackToWindowSelection()  // Navigate to WindowSelectionActivity
        }

        // Handle the Logout button logic
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // Function to show the confirmation dialog before logging out
    private fun showLogoutConfirmationDialog() {
        // Create an AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setCancelable(false)  // Prevents the dialog from being dismissed by tapping outside
            .setPositiveButton("Yes") { dialog, id ->
                navigateBackToLogin()  // Navigate back to MainActivity (Login page)
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()  // Dismiss the dialog if user chooses No
            }

        // Show the AlertDialog
        val alert = builder.create()
        alert.show()
    }

    // Navigate back to MainActivity (Login page)
    private fun navigateBackToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Close current activity to ensure the user can't navigate back to it
    }

    // Navigate back to WindowSelectionActivity
    private fun navigateBackToWindowSelection() {
        val intent = Intent(this, WindowSelectionActivity::class.java)
        startActivity(intent)
        finish()  // Close current activity
    }
}
