package com.example.appque

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow2Binding
import com.google.firebase.database.*

class StudentWindow2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow2Binding
    private lateinit var database: DatabaseReference
    private var currentQueueNumber = 0
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentWindow2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase database node for window2Queue
        database = FirebaseDatabase.getInstance().reference.child("window2Queue")
        userName = intent.getStringExtra("userName") ?: "Unknown User"


        // Fetch the current queue number
        fetchCurrentQueueNumber()

        // Button functionality
        binding.backArrowButton.setOnClickListener { finish() }

        binding.addButton.setOnClickListener {
            val selectedAppointment = binding.spinnerAppointmentOptions.selectedItem.toString()
            if (selectedAppointment == "Select") {
                Toast.makeText(this, "Please select an appointment type.", Toast.LENGTH_SHORT).show()
            } else {
                addAppointment(selectedAppointment)
            }
        }

        // Real-time listener for queue updates
        database.child("appointments").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateDisplay(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@StudentWindow2Activity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchCurrentQueueNumber() {
        database.child("currentQueueNumber").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentQueueNumber = snapshot.getValue(Int::class.java) ?: 0
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@StudentWindow2Activity,
                    "Error fetching queue number: ${error.message}",
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
}
