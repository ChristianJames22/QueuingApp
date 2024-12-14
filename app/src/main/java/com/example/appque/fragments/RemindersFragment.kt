package com.example.appque.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appque.adapters.RemindersAdapter
import com.example.appque.databinding.FragmentRemindersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private var database: DatabaseReference? = null
    private val remindersList = mutableListOf<Reminder>()
    private var adapter: RemindersAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("reminders")

        // Set up RecyclerView
        setupRecyclerView()

        // Load reminders from Firebase
        loadRemindersFromFirebase()
    }

    private fun setupRecyclerView() {
        adapter = RemindersAdapter(remindersList) { reminder ->
            // Show the details of the clicked reminder in a dialog
            showReminderDetails(reminder)
        }
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.staffRecyclerView.adapter = adapter
    }

    private fun loadRemindersFromFirebase() {
        try {
            database?.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        remindersList.clear()
                        for (data in snapshot.children) {
                            try {
                                val reminder = data.getValue(Reminder::class.java)
                                if (reminder != null) {
                                    remindersList.add(reminder)
                                }
                            } catch (e: Exception) {
                                Log.e("LoadReminders", "Error parsing reminder data: ${e.message}")
                            }
                        }
                        adapter?.notifyDataSetChanged()
                    } catch (e: Exception) {
                        Log.e("LoadReminders", "Error processing snapshot data: ${e.message}")
                        Toast.makeText(context, "An error occurred while processing reminders: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LoadReminders", "Database error: ${error.message}")
                    Toast.makeText(context, "Failed to load reminders: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("LoadReminders", "Unexpected error: ${e.message}")
            Toast.makeText(context, "An unexpected error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showReminderDetails(reminder: Reminder) {
        val dialogContent = """
            Title: ${reminder.title}
            Description: ${reminder.description}
            Date: ${reminder.date}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Reminder Details")
            .setMessage(dialogContent)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
