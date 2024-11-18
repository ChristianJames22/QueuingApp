package com.example.appque.fragments

import Student
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appque.MainActivity
import com.example.appque.R
import com.example.appque.StudentsAdapter
import com.google.firebase.auth.FirebaseAuth

class StudentsFragment : Fragment() {

    private lateinit var studentsAdapter: StudentsAdapter
    private val studentsList = mutableListOf<Student>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_students, container, false)

        // Setup RecyclerView and Adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.studentsRecyclerView)
        studentsAdapter = StudentsAdapter(studentsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = studentsAdapter

        // Settings button click listener
        val settingsButton: ImageButton = view.findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showSettingsMenu(settingsButton)
        }

        return view
    }

    private fun showSettingsMenu(anchor: ImageButton) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut() // Clear Firebase session
                navigateToLogin()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
