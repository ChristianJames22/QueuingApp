package com.example.appque

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityWindow1Binding
import com.google.firebase.auth.FirebaseAuth

class Window1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityWindow1Binding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inflate the layout using view binding
        binding = ActivityWindow1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val toggledId = intent.getStringExtra("toggledId")

        // Access the settings button and other views through binding
        binding.settingsButton.setOnClickListener {
            showSettingsMenu()
        }

        // Example: Accessing other views using binding
        binding.tvServingLabel.text = "Serving now..." // Set text programmatically
        binding.resetButton.setOnClickListener {
            Toast.makeText(this, "Reset clicked", Toast.LENGTH_SHORT).show()
        }
        binding.nextButton.setOnClickListener {
            Toast.makeText(this, "Next clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to show the settings menu with a profile and logout option
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
        // Logic for navigating to profile activity, modify as needed
        Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun performLogout() {
        auth.signOut()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
}
