package com.example.appque.fragments

import InactiveAdapter
import User
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appque.databinding.FragmentInactiveBinding
import com.google.firebase.database.*

class InactiveFragment : Fragment() {

    private var _binding: FragmentInactiveBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var adapter: InactiveAdapter
    private val inactiveList = mutableListOf<User>()
    private val filteredList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInactiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase reference to "inactive" node
        database = FirebaseDatabase.getInstance().getReference("inactive")

        // Set up RecyclerView with the adapter
        adapter = InactiveAdapter { user ->
            showRestoreConfirmationDialog(user)
        }
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.staffRecyclerView.adapter = adapter

        // Add TextWatcher to handle search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterInactiveUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Fetch and display inactive users
        fetchInactiveUsers()
    }

    private fun fetchInactiveUsers() {
        // Show progress indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyListTextView.visibility = View.GONE

        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(User::class.java)?.apply {
                    timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                        ?: System.currentTimeMillis()
                }
                user?.let {
                    inactiveList.add(0, it) // Add new user to the top of the list
                    filteredList.add(0, it) // Ensure filtered list stays updated
                    adapter.submitList(filteredList.toList()) // Submit new list to adapter
                    binding.emptyListTextView.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedUser = snapshot.getValue(User::class.java)?.apply {
                    timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                        ?: System.currentTimeMillis()
                }
                updatedUser?.let { user ->
                    val index = inactiveList.indexOfFirst { it.uid == user.uid }
                    if (index != -1) {
                        inactiveList[index] = user
                        filteredList[index] = user
                        adapter.submitList(filteredList.toList())
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val userId = snapshot.key
                userId?.let {
                    inactiveList.removeAll { user -> user.uid == userId }
                    filteredList.removeAll { user -> user.uid == userId }
                    adapter.submitList(filteredList.toList())
                    if (inactiveList.isEmpty()) {
                        binding.emptyListTextView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterInactiveUsers(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredList.clear()

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(inactiveList)
        } else {
            inactiveList.forEach { user ->
                if (user.name?.lowercase()?.contains(lowerCaseQuery) == true ||
                    user.email?.lowercase()?.contains(lowerCaseQuery) == true ||
                    user.id?.lowercase()?.contains(lowerCaseQuery) == true
                ) {
                    filteredList.add(user)
                }
            }
        }

        // Update the adapter with the filtered list
        adapter.submitList(filteredList.toList())

        // Update visibility for the empty list message
        binding.emptyListTextView.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showRestoreConfirmationDialog(user: User) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Restore User")
        builder.setMessage("Are you sure you want to restore ${user.name} to active users?")

        builder.setPositiveButton("Yes") { _, _ ->
            restoreUser(user)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun restoreUser(user: User) {
        val userId = user.uid ?: return

        try {
            binding.progressBar.visibility = View.VISIBLE

            // Move user from "inactive" to "users" node
            database.child(userId).removeValue().addOnSuccessListener {
                try {
                    FirebaseDatabase.getInstance().getReference("users").child(userId).setValue(user)
                        .addOnSuccessListener {
                            try {
                                // Remove from inactive list and update UI
                                inactiveList.remove(user)
                                filteredList.remove(user)
                                adapter.submitList(filteredList.toList())

                                binding.progressBar.visibility = View.GONE
                                binding.emptyListTextView.visibility = if (inactiveList.isEmpty()) View.VISIBLE else View.GONE
                                Toast.makeText(requireContext(), "${user.name} restored successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), "Error updating UI: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Failed to restore user: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error adding user to 'users' node: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to remove user from inactive: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
