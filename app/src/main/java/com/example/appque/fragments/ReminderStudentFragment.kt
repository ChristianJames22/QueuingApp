package com.example.appque.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

        // Initialize RecyclerView and Adapter
        adapter = RemindersAdapter(remindersList)
        binding.remindersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.remindersRecyclerView.adapter = adapter

        // Load reminders from the database
        loadRemindersFromDatabase()

        // Set click listener for add reminder button
        binding.addRemindersButton.setOnClickListener {
            showAddReminderDialog()
        }
    }

    private fun loadRemindersFromDatabase() {
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
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
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
            hint = "Title"
        }
        inputLayout.addView(titleInput)

        builder.setView(inputLayout)

        builder.setPositiveButton("Add") { _, _ ->
            val title = titleInput.text.toString()
            if (title.isNotEmpty()) {
                val reminderId = database.push().key ?: return@setPositiveButton

                // Get the current timestamp
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val reminder = Reminder(id = reminderId, title = title, time = currentTime)
                database.child(reminderId).setValue(reminder).addOnCompleteListener { task ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
