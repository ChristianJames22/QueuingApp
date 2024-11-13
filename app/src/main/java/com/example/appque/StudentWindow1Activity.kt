package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class StudentWindow1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_window1) // Your window 1 layout

        // Retrieve the username passed from MainActivity
        val name = intent.getStringExtra("name")

        // Set the username in the TextView inside the profile section
        val userNameTextView: TextView = findViewById(R.id.userNameTextView)
        if (name != null) {
            userNameTextView.text = name  // Set the username as the TextView text
        }

        // Handle the Back Arrow Button logic
        val backArrowButton = findViewById<ImageButton>(R.id.backArrowButton)
        backArrowButton.setOnClickListener {
            navigateBackToWindowSelection()  // Navigate to WindowSelectionActivity
        }

        // Handle the Settings button logic
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showSettingsMenu(settingsButton)
        }
    }

    // Function to show the settings menu with a logout option
    private fun showSettingsMenu(anchor: ImageButton) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // Function to show the confirmation dialog before logging out
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                navigateBackToLogin()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun navigateBackToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Navigate back to WindowSelectionActivity
    private fun navigateBackToWindowSelection() {
        val intent = Intent(this, WindowSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}
