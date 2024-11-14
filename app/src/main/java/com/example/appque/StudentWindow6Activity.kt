package com.example.appque

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow6Binding  // Correct ViewBinding for StudentWindow6Activity

class StudentWindow6Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow6Binding  // Correct ViewBinding reference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityStudentWindow6Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle the Back Arrow Button logic
        binding.backArrowButton.setOnClickListener {
            onBackPressed()  // Calls the overridden onBackPressed() method to navigate back
        }

        // Handle the Settings button logic
        binding.settingsButton.setOnClickListener {
            showSettingsMenu(binding.settingsButton)  // Pass the settings button to show the menu
        }
    }

    // Override onBackPressed to just go back to the previous activity in the back stack
    override fun onBackPressed() {
        super.onBackPressed()  // This takes the user back to the previous activity in the stack
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
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun navigateBackToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
