package com.example.appque

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentCashierBinding
import com.google.firebase.database.*

class StudentCashierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentCashierBinding
    private lateinit var database: FirebaseDatabase
    private var currentQueueNumber = 0
    private var userName: String? = null // To store the user's name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentCashierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance()

        // Retrieve the user's name from intent
        userName = intent.getStringExtra("userName") ?: "Unknown User"

        // Fetch the current queue number from Firebase
        fetchCurrentQueueNumber()

        // Back arrow button functionality
        binding.backArrowButton.setOnClickListener {
            finish()
        }

        // Add appointment button functionality
        binding.addButton.setOnClickListener {
            val selectedAppointment = binding.spinnerAppointmentOptions.selectedItem.toString()
            if (selectedAppointment == "Select") {
                Toast.makeText(this, "Please select an appointment type.", Toast.LENGTH_SHORT).show()
            } else {
                addAppointment(selectedAppointment)
            }
        }

        // Real-time listener for appointment queue updates
        database.getReference("queue").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateDisplay(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@StudentCashierActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchCurrentQueueNumber() {
        // Fetch the last known queue number from Firebase
        database.getReference("queue").child("currentQueueNumber")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // If a queue number exists, set it; otherwise, default to 0
                    currentQueueNumber = snapshot.getValue(Int::class.java) ?: 0
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentCashierActivity,
                        "Error fetching queue number: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun addAppointment(selectedAppointment: String) {
        // Increment queue number
        currentQueueNumber++

        // Create appointment entry
        val appointment =
            "Name: $userName - $selectedAppointment\nQueue No: $currentQueueNumber\n"
        // Push the appointment to the database
        database.getReference("queue").child("appointments").push().setValue(appointment)
        database.getReference("queue").child("currentQueueNumber").setValue(currentQueueNumber)

        // Notify the user
        Toast.makeText(this, "Appointment added to queue.", Toast.LENGTH_SHORT).show()
    }

    private fun updateDisplay(snapshot: DataSnapshot) {
        // Fetch appointments from the database
        val appointments = snapshot.child("appointments").children.map { it.value.toString() }

        // Update the "Serving Now" and "Next" displays
        if (appointments.isEmpty()) {
            binding.tvServingNow.text = "No appointment"
            binding.tvNextInLine.text = ""
        } else {
            binding.tvServingNow.text = appointments.firstOrNull()
            binding.tvNextInLine.text =
                appointments.drop(1).joinToString("\n")
        }
    }
}
