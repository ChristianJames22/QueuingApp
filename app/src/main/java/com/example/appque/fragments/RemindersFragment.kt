package com.example.appque.fragments

import android.os.Bundle
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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                remindersList.clear()
                for (data in snapshot.children) {
                    val reminder = data.getValue(Reminder::class.java)
                    if (reminder != null) {
                        remindersList.add(reminder)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load reminders: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
