package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityWindow7Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Window7Activity : AppCompatActivity() {

    private lateinit var binding: ActivityWindow7Binding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWindow7Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("window7Queue") // Node for Window7

        // Real-time listener for queue updates
        database.child("appointments").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateDisplay(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Window7Activity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Set up button listeners
        binding.nextButton.setOnClickListener {
            database.child("appointments").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    showNextConfirmationDialog()
                } else {
                    Toast.makeText(this, "No appointments in the queue.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.resetButton.setOnClickListener {
            database.child("appointments").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    showResetConfirmationDialog()
                } else {
                    Toast.makeText(this, "No appointments to reset.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fetchUserData()

        // Settings button functionality
        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            showSettingsMenu()
        }
    }

    private fun showNextConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Next")
            .setMessage("Are you sure you want to move to the next appointment?")
            .setPositiveButton("Yes") { _, _ ->
                moveToNextAppointment()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun moveToNextAppointment() {
        database.child("appointments").get().addOnSuccessListener { snapshot ->
            val appointments = snapshot.children.toList()
            if (appointments.isNotEmpty()) {
                val firstKey = appointments.first().key
                firstKey?.let { database.child("appointments").child(it).removeValue() }
                Toast.makeText(this, "Moved to the next appointment.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to move to the next appointment.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Reset")
            .setMessage("Are you sure you want to reset the queue?")
            .setPositiveButton("Yes") { _, _ ->
                resetQueue()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun resetQueue() {
        database.child("appointments").removeValue()
        database.child("currentQueueNumber").setValue(0)
            .addOnSuccessListener {
                Toast.makeText(this, "Queue has been reset and starts from 1.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to reset the queue.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            FirebaseDatabase.getInstance().reference.child("users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val userName = snapshot.child("name").value?.toString() ?: "Unknown"
                        val userIdNumber = snapshot.child("id").value?.toString() ?: "N/A"
                        val userCourse = snapshot.child("course").value?.toString() ?: "N/A"
                        val userYear = snapshot.child("year").value?.toString() ?: "N/A"

                        findViewById<TextView>(R.id.textName)?.text = "Name: $userName"
                        findViewById<TextView>(R.id.textIdNumber)?.text = "ID No.: $userIdNumber"
                        findViewById<TextView>(R.id.textCourse)?.text = "Course: $userCourse"
                        findViewById<TextView>(R.id.textYear)?.text = "Year: $userYear"
                    }
                }
        }
    }

    private fun updateDisplay(snapshot: DataSnapshot) {
        val appointments = snapshot.children.map { it.value.toString() }
        if (appointments.isEmpty()) {
            binding.tvServingNow.text = "No appointment"
            binding.tvNextInLine.text = ""
        } else {
            binding.tvServingNow.text = appointments.firstOrNull()
            binding.tvNextInLine.text = appointments.drop(1).joinToString("\n")
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
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            FirebaseDatabase.getInstance().reference.child("users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
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
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
    }
}
