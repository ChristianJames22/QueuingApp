package com.example.appque;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.appque.databinding.ActivityWindow7Binding
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

class Window7Activity : AppCompatActivity() {

    private var currentServingAppointment: String? = null
    private lateinit var binding: ActivityWindow7Binding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isOnBreak = false
    private var isOffline = false
    private val appointmentsList = mutableListOf<String>()
    private lateinit var appointmentAdapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWindow7Binding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        appointmentAdapter = AppointmentAdapter(appointmentsList)
        binding.appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Window7Activity)
            adapter = appointmentAdapter
        }

        // Fetch persisted statuses
        fetchWindow7Status()

        // Real-time listener for queue status
        database.child("window7Queue").child("appointments").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                appointmentsList.clear()
                if (snapshot.exists()) {
                    val appointments = snapshot.children.map { it.value.toString() }
                    if (appointments.isNotEmpty()) {
                        currentServingAppointment = appointments[0]
                        if (!isOnBreak && !isOffline) {
                            binding.tvServingNow.text = currentServingAppointment
                        }
                        appointmentsList.addAll(appointments.drop(1))
                    } else {
                        currentServingAppointment = null
                        if (!isOnBreak && !isOffline) {
                            binding.tvServingNow.text = "No Appointment"
                        }
                    }
                } else {
                    currentServingAppointment = null
                    if (!isOnBreak && !isOffline) {
                        binding.tvServingNow.text = "No Appointment"
                    }
                }
                appointmentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Window7Activity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Button listeners
        binding.onBreakButton.setOnClickListener { confirmToggleOnBreak() }
        binding.offlineButton.setOnClickListener { confirmToggleOffline() }
        binding.nextButton.setOnClickListener { moveToNextAppointment() }
        binding.resetButton.setOnClickListener { resetQueue() }
        binding.settingsButton.setOnClickListener { showSettingsMenu() }
    }

    private fun fetchWindow7Status() {
        val window7Id = "window7"
        database.child("window7Status").child(window7Id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isOnBreak = snapshot.child("onBreak").getValue(Boolean::class.java) ?: false
                isOffline = snapshot.child("offline").getValue(Boolean::class.java) ?: false
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Window7Activity, "Failed to fetch window 7 status: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateFirebaseStatus(isOnBreak: Boolean? = null, isOffline: Boolean? = null) {
        val window7Id = "window7"
        isOnBreak?.let { database.child("window7Status").child(window7Id).child("onBreak").setValue(it) }
        isOffline?.let { database.child("window7Status").child(window7Id).child("offline").setValue(it) }
    }

    private fun updateUI() {
        binding.tvServingNow.apply {
            text = when {
                isOnBreak -> "ON BREAK"
                isOffline -> "OFFLINE"
                else -> currentServingAppointment ?: "No Appointment"
            }
            setTextColor(
                when {
                    isOnBreak || isOffline -> Color.RED
                    else -> Color.BLACK
                }
            )
        }

        binding.onBreakButton.apply {
            text = if (isOnBreak) "On Break: ON" else "On Break: OFF"
            setBackgroundColor(if (isOnBreak) Color.GREEN else Color.LTGRAY)
            isEnabled = !isOffline
        }

        binding.offlineButton.apply {
            text = if (isOffline) "Offline: ON" else "Offline: OFF"
            setBackgroundColor(if (isOffline) Color.GREEN else Color.LTGRAY)
            isEnabled = !isOnBreak
        }

        binding.nextButton.isEnabled = !isOnBreak && !isOffline
        binding.resetButton.isEnabled = !isOnBreak && !isOffline
    }

    private fun confirmToggleOnBreak() {
        val message = if (isOnBreak) "turn off On Break?" else "set to On Break?"
        AlertDialog.Builder(this)
            .setTitle("Confirm On Break")
            .setMessage("Are you sure you want to $message")
            .setPositiveButton("Yes") { _, _ -> toggleOnBreak() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun confirmToggleOffline() {
        val message = if (isOffline) "turn off Offline?" else "set to Offline?"
        AlertDialog.Builder(this)
            .setTitle("Confirm Offline")
            .setMessage("Are you sure you want to $message")
            .setPositiveButton("Yes") { _, _ -> toggleOffline() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun toggleOnBreak() {
        isOnBreak = !isOnBreak
        updateFirebaseStatus(isOnBreak = isOnBreak)
        Toast.makeText(this, if (isOnBreak) "Window 7 is now On Break." else "Welcome back!", Toast.LENGTH_SHORT).show()
        updateUI()
    }

    private fun toggleOffline() {
        isOffline = !isOffline
        updateFirebaseStatus(isOffline = isOffline)
        Toast.makeText(this, if (isOffline) "Window 7 is now Offline." else "You are now Online.", Toast.LENGTH_SHORT).show()
        updateUI()
    }

    private fun resetQueue() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Reset")
            .setMessage("Are you sure you want to reset the queue?")
            .setPositiveButton("Yes") { _, _ ->
                database.child("window7Queue").child("appointments").removeValue()
                database.child("window7Queue").child("currentQueueNumber").setValue(0)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Queue has been reset.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun moveToNextAppointment() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Next Appointment")
            .setMessage("Are you sure you want to move to the next appointment?")
            .setPositiveButton("Yes") { _, _ ->
                if (isOnBreak || isOffline) {
                    Toast.makeText(this, "Cannot process while On Break or Offline.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                database.child("window7Queue").child("appointments").get().addOnSuccessListener { snapshot ->
                    val appointments = snapshot.children.toList()
                    if (appointments.isNotEmpty()) {
                        val firstKey = appointments.first().key
                        firstKey?.let {
                            database.child("window7Queue").child("appointments").child(it).removeValue()
                            Toast.makeText(this, "Moved to the next appointment.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No appointments in the queue.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
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
                    } else {
                        Toast.makeText(this, "No profile data found.", Toast.LENGTH_SHORT).show()
                    }
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

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
    }
}