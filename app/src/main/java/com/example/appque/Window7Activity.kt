package com.example.appque

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityWindow1Binding
import com.example.appque.databinding.ActivityWindow7Binding

class Window7Activity : AppCompatActivity() {

    private lateinit var binding: ActivityWindow7Binding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding
        binding = ActivityWindow7Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val toggledId = intent.getStringExtra("toggledId")

        // Access the settings button and other views through binding
        binding.settingsButton.setOnClickListener {
            showSettingsMenu(binding.settingsButton)
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
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }
}
