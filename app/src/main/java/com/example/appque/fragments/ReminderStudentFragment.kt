package com.example.appque.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appque.R
import com.example.appque.adapters.RemindersAdapter
import com.example.appque.databinding.ActivityReminderStudentFragmentBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ReminderStudentFragment<T> : Fragment() {

    private var _binding: ActivityReminderStudentFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private val remindersList = mutableListOf<Reminder>()
    private lateinit var adapter: RemindersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityReminderStudentFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("reminders")

        // Set up RecyclerView and Adapter
        setupRecyclerView()

        // Load reminders from Firebase
        loadRemindersFromDatabase()

        // Add reminder button click listener
        binding.addReminderButton.setOnClickListener {
            showAddReminderDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = RemindersAdapter(remindersList) { reminder ->
            showEditDeleteOptions(reminder)
        }
        binding.remindersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.remindersRecyclerView.adapter = adapter
    }

    private fun loadRemindersFromDatabase() {
        // Show progress bar when loading begins
        binding.progressBar.visibility = View.VISIBLE

        database.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                remindersList.clear()
                for (data in snapshot.children) {
                    val reminder = data.getValue(Reminder::class.java)
                    if (reminder != null) {
                        remindersList.add(reminder)
                    }
                }
                adapter.notifyDataSetChanged()

                // Hide progress bar after loading
                binding.progressBar.visibility = View.GONE

                // Show or hide empty list text
                binding.emptyListTextView.visibility =
                    if (remindersList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                binding.progressBar.visibility = View.GONE // Hide progress bar on failure
                Toast.makeText(context, "Failed to load reminders: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddReminderDialog() {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Add Reminder")

        val inputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val titleInput = android.widget.EditText(context).apply {
            hint = "Reminder"
        }
        inputLayout.addView(titleInput)

        builder.setView(inputLayout)

        builder.setPositiveButton("Add") { _, _ ->
            val title = titleInput.text.toString()
            if (title.isNotEmpty()) {
                val reminderId = database.push().key ?: return@setPositiveButton

                // Show progress bar
                binding.progressBar.visibility = View.VISIBLE

                // Get the current timestamp
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val reminder = Reminder(id = reminderId, title = title, time = currentTime)
                database.child(reminderId).setValue(reminder).addOnCompleteListener { task ->
                    // Hide progress bar
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        Toast.makeText(context, "Reminder added", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to add reminder", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }



    private fun showEditDeleteOptions(reminder: Reminder) {
        val builder = android.app.AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reminder_details, null)
        builder.setView(dialogView)

        val alertDialog = builder.create()

        // Bind reminder details TextView
        val reminderDetailsTextView = dialogView.findViewById<TextView>(R.id.reminderDetailsTextView)
        reminderDetailsTextView.text = "Reminder: \n${reminder.title}\nTime: ${reminder.time}"

        // Bind buttons
        val updateButton = dialogView.findViewById<Button>(R.id.updateButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

        // Set button click listeners
        updateButton.setOnClickListener {
            alertDialog.dismiss() // Close the dialog before opening another
            showEditReminderDialog(reminder)
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(reminder) { success ->
                if (success) {
                    alertDialog.dismiss() // Close the dialog after successful deletion
                }
            }
        }

        alertDialog.show()
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Edit Reminder")

        val inputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val titleInput = android.widget.EditText(context).apply {
            hint = "Reminder"
            setText(reminder.title)
        }
        inputLayout.addView(titleInput)

        builder.setView(inputLayout)

        builder.setPositiveButton("Save") { _, _ ->
            val updatedTitle = titleInput.text.toString()
            if (updatedTitle.isNotEmpty()) {
                // Show progress bar
                binding.progressBar.visibility = View.VISIBLE

                // Get the updated timestamp
                val updatedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val updatedReminder = reminder.copy(title = updatedTitle, time = updatedTime)
                database.child(reminder.id).setValue(updatedReminder).addOnCompleteListener { task ->
                    // Hide progress bar
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        Toast.makeText(context, "Reminder updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update reminder", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }


    private fun showDeleteConfirmationDialog(reminder: Reminder, onDelete: (Boolean) -> Unit) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Yes") { _, _ ->
                // Show progress bar
                binding.progressBar.visibility = View.VISIBLE

                database.child(reminder.id).removeValue().addOnCompleteListener { task ->
                    // Hide progress bar
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show()
                        onDelete(true) // Notify successful deletion
                    } else {
                        Toast.makeText(context, "Failed to delete reminder", Toast.LENGTH_SHORT).show()
                        onDelete(false) // Notify failed deletion
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
