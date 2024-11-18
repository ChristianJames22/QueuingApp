package com.example.appque

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CashierActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cashier)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Get data passed through the intent
        val userName = intent.getStringExtra("name") ?: "Guest"
        val toggledId = intent.getStringExtra("toggledId") ?: "N/A"

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
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Logout function to clear the session and navigate to the MainActivity
    private fun logout() {
        // Firebase logout
        auth.signOut()

        // Clear activity stack and navigate to login screen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
