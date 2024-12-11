package com.example.appque;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.appque.databinding.ActivityStudentWindow8Binding;
import com.google.firebase.database.*;

class StudentWindow8Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow8Binding;
    private lateinit var database: DatabaseReference;
    private lateinit var studentAppointmentAdapter: StudentAppointmentAdapter;
    private var currentQueueNumber = 0;
    private var userName: String? = null;
    private val appointmentsList = mutableListOf<String>();
    private var isOnBreak = false;
    private var isOffline = false;
    private var hasActiveAppointment = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentWindow8Binding.inflate(layoutInflater);
        setContentView(binding.root);

        database = FirebaseDatabase.getInstance().reference;
        userName = intent.getStringExtra("userName") ?: "Unknown User";

        // Initialize RecyclerView and Adapter
        studentAppointmentAdapter = StudentAppointmentAdapter();
        binding.appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StudentWindow8Activity);
            adapter = studentAppointmentAdapter;
        }

        fetchCurrentQueueNumber();
        setupRealTimeQueueListener();
        observeWindow8Status();
        setupRealTimeUserActiveAppointmentListener();

        binding.addButton.setOnClickListener { handleAddAppointment(); };
        binding.backArrowButton.setOnClickListener { finish(); };
    }

    private fun fetchCurrentQueueNumber() {
        database.child("window8Queue").child("currentQueueNumber")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentQueueNumber = snapshot.getValue(Int::class.java) ?: 0;
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentWindow8Activity,
                        "Error fetching queue number: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }

    private fun setupRealTimeUserActiveAppointmentListener() {
        database.child("window8Queue").child("appointments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hasActiveAppointment = snapshot.children.any {
                        it.value.toString().contains(userName ?: "");
                    };
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentWindow8Activity,
                        "Error checking active appointment: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }

    private fun observeWindow8Status() {
        val cashierId = "window8" // ID for the cashier node
        database.child("window8Status").child(cashierId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isOnBreak = snapshot.child("onBreak").getValue(Boolean::class.java) ?: false
                isOffline = snapshot.child("offline").getValue(Boolean::class.java) ?: false

                when {
                    isOffline -> {
                        updateUI("OFFLINE", false, hideRecyclerView = true, textColor = android.R.color.holo_red_dark)
                    }
                    isOnBreak -> {
                        updateUI("ON BREAK", false, hideRecyclerView = false, textColor = android.R.color.holo_red_dark)
                    }
                    else -> {
                        updateUI("ONLINE", true, hideRecyclerView = false, textColor = android.R.color.black)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@StudentWindow8Activity,
                    "Failed to fetch window 1 status: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateUI(status: String, enableButton: Boolean, hideRecyclerView: Boolean, textColor: Int) {
        binding.tvWindow1Status.apply {
            text = status
            setTextColor(resources.getColor(textColor, null))
        }
        binding.addButton.isEnabled = enableButton

        binding.appointmentsRecyclerView.visibility = if (hideRecyclerView) View.GONE else View.VISIBLE

        if (!enableButton && status == "ON BREAK") {
            binding.tvWindow1Status.text = "ON BREAK"
        } else if (!enableButton && status == "OFFLINE") {
            binding.tvWindow1Status.text = "OFFLINE"
        } else {
            updateQueueDisplay()
        }
    }

    private fun setupRealTimeQueueListener() {
        database.child("window8Queue").child("appointments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointmentsList.clear();
                    snapshot.children.forEach { appointmentsList.add(it.value.toString()); };

                    if (!isOffline) {
                        updateQueueDisplay();
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentWindow8Activity,
                        "Error fetching appointments: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }

    private fun handleAddAppointment() {
        // Check if the window is offline, on break, or the user already has an active appointment
        when {
            isOffline -> {
                Toast.makeText(this, "Cannot add appointments. Window is OFFLINE.", Toast.LENGTH_LONG).show();
                return;
            }
            isOnBreak -> {
                Toast.makeText(this, "Cannot add appointments. Window is ON BREAK.", Toast.LENGTH_LONG).show();
                return;
            }
            hasActiveAppointment -> {
                Toast.makeText(this, "You already have an active appointment.", Toast.LENGTH_LONG).show();
                return;
            }
        };

        // Ensure a valid appointment type is selected
        val selectedAppointment = binding.spinnerAppointmentOptions.selectedItem.toString();
        if (selectedAppointment == "Select") {
            Toast.makeText(this, "Please select an appointment type.", Toast.LENGTH_SHORT).show();
            return;
        };

        // If all conditions are met, confirm adding the appointment
        confirmAddAppointment(selectedAppointment);
    }

    private fun confirmAddAppointment(appointmentType: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Appointment")
            .setMessage("Do you want to add the appointment: $appointmentType?")
            .setPositiveButton("Yes") { _, _ -> addAppointment(appointmentType); }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss(); }
            .create()
            .show();
    }

    private fun addAppointment(selectedAppointment: String) {
        currentQueueNumber++;
        val appointment = "Name: $userName - $selectedAppointment\nQueue No: $currentQueueNumber\n";

        database.child("window8Queue").child("appointments").push().setValue(appointment)
            .addOnSuccessListener {
                database.child("window8Queue").child("currentQueueNumber").setValue(currentQueueNumber);
                hasActiveAppointment = true;
                Toast.makeText(this, "Appointment added to the queue.", Toast.LENGTH_SHORT).show();
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add appointment: ${it.message}", Toast.LENGTH_SHORT).show();
            };
    }

    private fun updateQueueDisplay() {
        if (appointmentsList.isNotEmpty()) {
            val currentQueue = appointmentsList.firstOrNull();
            binding.tvWindow1Status.text = currentQueue ?: "No Appointment";
            val remainingAppointments = appointmentsList.drop(1);
            studentAppointmentAdapter.submitList(remainingAppointments);
        } else {
            binding.tvWindow1Status.text = "No Appointment";
            studentAppointmentAdapter.submitList(emptyList());
        }
    }
}
