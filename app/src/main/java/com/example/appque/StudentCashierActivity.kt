package com.example.appque;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.appque.databinding.ActivityStudentCashierBinding;
import com.google.firebase.database.*;

class StudentCashierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentCashierBinding;
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
        binding = ActivityStudentCashierBinding.inflate(layoutInflater);
        setContentView(binding.root);

        database = FirebaseDatabase.getInstance().reference;
        userName = intent.getStringExtra("userName") ?: "Unknown User";

        // Initialize RecyclerView and Adapter
        studentAppointmentAdapter = StudentAppointmentAdapter();
        binding.appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StudentCashierActivity);
            adapter = studentAppointmentAdapter;
        };

        fetchCurrentQueueNumber();
        setupRealTimeQueueListener();
        observeCashierStatus();
        setupRealTimeUserActiveAppointmentListener();

        binding.addButton.setOnClickListener { handleAddAppointment() };
        binding.backArrowButton.setOnClickListener { finish() };
    }

    private fun fetchCurrentQueueNumber() {
        database.child("window1Queue").child("currentQueueNumber")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentQueueNumber = snapshot.getValue(Int::class.java) ?: 0;
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentCashierActivity,
                        "Error fetching queue number: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }

    private fun setupRealTimeUserActiveAppointmentListener() {
        database.child("window1Queue").child("appointments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hasActiveAppointment = snapshot.children.any {
                        it.value.toString().contains(userName ?: "")
                    };
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentCashierActivity,
                        "Error checking active appointment: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }

    private fun observeCashierStatus() {
        val cashierId = "cashier1"; // ID for the cashier node
        database.child("cashierStatus").child(cashierId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isOnBreak = snapshot.child("onBreak").getValue(Boolean::class.java) ?: false;
                isOffline = snapshot.child("offline").getValue(Boolean::class.java) ?: false;

                when {
                    isOffline -> {
                        updateUI("OFFLINE", false, hideRecyclerView = true);
                    }
                    isOnBreak -> {
                        updateUI("ON BREAK", false, hideRecyclerView = false);
                    }
                    else -> {
                        updateUI("ONLINE", true, hideRecyclerView = false);
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@StudentCashierActivity,
                    "Failed to fetch cashier status: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private fun updateUI(status: String, enableButton: Boolean, hideRecyclerView: Boolean) {
        binding.tvCashierStatus.text = "Cashier is $status";
        binding.addButton.isEnabled = enableButton;

        if (hideRecyclerView) {
            binding.appointmentsRecyclerView.visibility = View.GONE;
        } else {
            binding.appointmentsRecyclerView.visibility = View.VISIBLE;
        }

        if (!enableButton) {
            binding.tvCashierStatus.text = "ON BREAK";
        } else {
            updateQueueDisplay();
        }
    }

    private fun setupRealTimeQueueListener() {
        database.child("window1Queue").child("appointments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointmentsList.clear();
                    snapshot.children.forEach { appointmentsList.add(it.value.toString()) };

                    if (!isOffline) {
                        updateQueueDisplay();
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudentCashierActivity,
                        "Error fetching appointments: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
    }

    private fun handleAddAppointment() {
        // Check if the cashier is offline, on break, or the user already has an active appointment
        when {
            isOffline -> {
                Toast.makeText(this, "Cannot add appointments. Cashier is OFFLINE.", Toast.LENGTH_LONG).show();
                return;
            }
            isOnBreak -> {
                Toast.makeText(this, "Cannot add appointments. Cashier is ON BREAK.", Toast.LENGTH_LONG).show();
                return;
            }
            hasActiveAppointment -> {
                Toast.makeText(this, "You already have an active appointment.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Ensure a valid appointment type is selected
        val selectedAppointment = binding.spinnerAppointmentOptions.selectedItem.toString();
        if (selectedAppointment == "Select") {
            Toast.makeText(this, "Please select an appointment type.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all conditions are met, confirm adding the appointment
        confirmAddAppointment(selectedAppointment);
    }

    private fun confirmAddAppointment(appointmentType: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Appointment")
            .setMessage("Do you want to add the appointment: $appointmentType?")
            .setPositiveButton("Yes") { _, _ -> addAppointment(appointmentType) }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show();
    }

    private fun addAppointment(selectedAppointment: String) {
        currentQueueNumber++;
        val appointment = "Name: $userName - $selectedAppointment\nQueue No: $currentQueueNumber\n";

        database.child("window1Queue").child("appointments").push().setValue(appointment)
            .addOnSuccessListener {
                database.child("window1Queue").child("currentQueueNumber").setValue(currentQueueNumber);
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
            binding.tvCashierStatus.text = currentQueue ?: "No Appointment";
            val remainingAppointments = appointmentsList.drop(1);
            studentAppointmentAdapter.submitList(remainingAppointments);
        } else {
            binding.tvCashierStatus.text = "No Appointment";
            studentAppointmentAdapter.submitList(emptyList());
        }
    }
}
