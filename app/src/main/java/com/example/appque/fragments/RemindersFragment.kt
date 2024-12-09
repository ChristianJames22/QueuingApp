package com.example.appque.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private lateinit var database: DatabaseReference
    private val remindersList = mutableListOf<Reminder>()
    private lateinit var adapter: RemindersAdapter

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
            // Do nothing on click; view-only mode
        }
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.staffRecyclerView.adapter = adapter
    }

    private fun loadRemindersFromFirebase() {
        try {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        remindersList.clear()
                        for (data in snapshot.children) {
                            try {
                                val reminder = data.getValue(Reminder::class.java)
                                if (reminder != null) {
                                    // Add reminders to the top of the list
                                    remindersList.add(0, reminder)
                                }
                            } catch (e: Exception) {
                                Log.e("LoadReminders", "Error parsing reminder data: ${e.message}")
                            }
                        }
                        adapter.notifyDataSetChanged()
                        binding.staffRecyclerView.scrollToPosition(0) // Scroll to the top after adding
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
