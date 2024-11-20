package com.example.appque.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appque.R
import com.example.appque.StudentCashierActivity
import com.example.appque.StudentWindow2Activity
import com.example.appque.StudentWindow3Activity
import com.example.appque.StudentWindow4Activity
import com.example.appque.StudentWindow5Activity
import com.example.appque.StudentWindow6Activity
import com.example.appque.StudentWindow7Activity
import com.example.appque.StudentWindow8Activity
import com.example.appque.databinding.FragmentWindowsBinding

class WindowsFragment : Fragment(R.layout.fragment_windows) {

    private var _binding: FragmentWindowsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = FragmentWindowsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button listeners
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.buttonCashier.setOnClickListener {
            navigateToActivity(StudentCashierActivity::class.java)
        }

        binding.buttonWindow2.setOnClickListener {
            navigateToActivity(StudentWindow2Activity::class.java)
        }
        binding.buttonWindow3.setOnClickListener {
            navigateToActivity(StudentWindow3Activity::class.java)
        }
        binding.buttonWindow4.setOnClickListener {
            navigateToActivity(StudentWindow4Activity::class.java)
        }
        binding.buttonWindow5.setOnClickListener {
            navigateToActivity(StudentWindow5Activity::class.java)
        }
        binding.buttonWindow6.setOnClickListener {
            navigateToActivity(StudentWindow6Activity::class.java)
        }
        binding.buttonWindow7.setOnClickListener {
            navigateToActivity(StudentWindow7Activity::class.java)
        }
        binding.buttonWindow8.setOnClickListener {
            navigateToActivity(StudentWindow8Activity::class.java)
        }
    }

    private fun navigateToActivity(destination: Class<*>) {
        Log.d("WindowsFragment", "Navigating to $destination")
        val intent = Intent(requireContext(), destination)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}