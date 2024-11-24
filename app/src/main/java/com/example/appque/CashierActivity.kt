package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityCashierBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CashierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCashierBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCashierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Fetch user data from Realtime Database
        fetchUserData()

        // Settings button functionality
        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            showSettingsMenu()
        }
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val userName = snapshot.child("name").value?.toString() ?: "Unknown"
                    val userIdNumber = snapshot.child("id").value?.toString() ?: "N/A"
                    val userCourse = snapshot.child("course").value?.toString() ?: "N/A"
                    val userYear = snapshot.child("year").value?.toString() ?: "N/A"

                    Log.d(
                        "CashierActivity",
                        "Fetched Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
                    )

                    // Display user data in UI
                    findViewById<TextView>(R.id.textName)?.text = "Name: $userName"
                    findViewById<TextView>(R.id.textIdNumber)?.text = "ID No.: $userIdNumber"
                    findViewById<TextView>(R.id.textCourse)?.text = "Course: $userCourse"
                    findViewById<TextView>(R.id.textYear)?.text = "Year: $userYear"
                }
                .addOnFailureListener { exception ->
                    Log.e("CashierActivity", "Failed to fetch user data: ${exception.message}")
                    Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
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
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val userName = snapshot.child("name").value?.toString() ?: "Unknown"
                    val userIdNumber = snapshot.child("id").value?.toString() ?: "N/A"
                    val userCourse = snapshot.child("course").value?.toString() ?: "N/A"
                    val userYear = snapshot.child("year").value?.toString() ?: "N/A"

                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("name", userName)
                        putExtra("id", userIdNumber)
                        putExtra("course", userCourse)
                        putExtra("year", userYear)
                    }
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load profile data.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                navigateToLogin()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
