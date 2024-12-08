package com.example.appque

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentCashierBinding
import com.google.firebase.database.*

class StudentCashierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentCashierBinding
    private lateinit var database: DatabaseReference
    private var currentQueueNumber = 0
    private var userName: String? = null
    private var userHasAppointment = false // Track if the user has an appointment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentCashierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database for queue
        database = FirebaseDatabase.getInstance().reference.child("window1Queue                                                                                                                                                                                                                                               ")
        userName = intent.getStringExtra("userName") ?: "Unknown User"

        // Fetch the current queue number
        fetchCurrentQueueNumber()

        // Check if the user already has an appointment
        checkUserAppointmentStatus()

        // Back button functionality
        binding.backArrowButton.setOnClickListener { finish() }

        // Add appointment button functionality
        binding.addButton.setOnClickListener {
            if (userHasAppointment) {
                // Show a prompt if the user already has an appointment
                Toast.makeText(
                    this,
                    "You already have an active appointment. You cannot add another.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val selectedAppointment = binding.spinnerAppointmentOptions.selectedItem.toString()
                if (selectedAppointment == "Select") {
                    Toast.makeText(this, "Please select an appointment type.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    addAppointment(selectedAppointment)
                }
            }
        }

        // Real-time listener for queue updates
        database.child("appointments").addValueEventListener(object : ValueEventListener {
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
        database.child("currentQueueNumber")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
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

    private fun checkUserAppointmentStatus() {
        // Check if the user already has an appointment in the queue
        database.child("appointments")
            .orderByValue()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if any appointment matches the current user's name
                    userHasAppointment = snapshot.children.any {
                        it.value.toString().contains(userName ?: "")
                    }

                    if (userHasAppointment) {
                        Toast.makeText(
                            this@StudentCashierActivity,
                            "You already have an active appointment.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentCashierActivity,
                        "Error checking appointment status: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun addAppointment(selectedAppointment: String) {
        currentQueueNumber++

        val appointment = "Name: $userName - $selectedAppointment\nQueue No: $currentQueueNumber\n"

        database.child("appointments").push().setValue(appointment)
        database.child("currentQueueNumber").setValue(currentQueueNumber)

        Toast.makeText(this, "Appointment added to queue.", Toast.LENGTH_SHORT).show()

        // Re-check the user's appointment status
        checkUserAppointmentStatus()
    }

    private fun updateDisplay(snapshot: DataSnapshot) {
        val appointments = snapshot.children.map { it.value.toString() }
        if (appointments.isEmpty()) {
            binding.tvServingNow.text = "No appointment"
            binding.tvNextInLine.text = ""
            // Re-enable the "Add Appointment" button if there are no appointments
            userHasAppointment = false
        } else {
            binding.tvServingNow.text = appointments.firstOrNull()
            binding.tvNextInLine.text = appointments.drop(1).joinToString("\n")
        }
    }
}
