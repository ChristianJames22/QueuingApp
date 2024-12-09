package com.example.appque.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appque.databinding.FragmentsMeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MeFragments : Fragment() {

    private var _binding: FragmentsMeBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentsMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assume the user ID is passed as an argument
        val userId = arguments?.getString("id") ?: "N/A"
        if (userId != "N/A") {
            fetchUserData(userId)
        } else {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserData(userId: String) {
        try {
            database.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            if (snapshot.exists()) {
                                val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                                val id = snapshot.child("id").getValue(String::class.java) ?: "N/A"
                                val course = snapshot.child("course").getValue(String::class.java) ?: "N/A"
                                val year = snapshot.child("year").getValue(String::class.java) ?: "N/A"

                                // Update UI with data
                                binding.textName.text = name // Only display the name
                                binding.textIdNumber.text = "ID No.: $id"
                                binding.textCourse.text = "Course: $course"
                                binding.textYear.text = "Year: $year"
                            } else {
                                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("FetchUserData", "Error processing user data: ${e.message}")
                            Toast.makeText(requireContext(), "An error occurred while processing user data.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FetchUserData", "Database error: ${error.message}")
                        Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Log.e("FetchUserData", "Unexpected error: ${e.message}")
            Toast.makeText(requireContext(), "An unexpected error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
