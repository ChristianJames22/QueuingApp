package com.example.appque.fragments

import Student
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appque.R
import com.example.appque.StudentsAdapter

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

        // Add button click listener
        val addStudentButton: ImageButton = view.findViewById(R.id.addStudentButton)
        addStudentButton.setOnClickListener {
            showAddStudentDialog()
        }

        // Settings button click listener
        val settingsButton: ImageButton = view.findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showSettingsMenu(settingsButton)
        }

        return view
    }

    private fun showAddStudentDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_student, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog = dialogBuilder.create()

        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val courseInput = dialogView.findViewById<EditText>(R.id.courseInput)
        val yearInput = dialogView.findViewById<EditText>(R.id.yearInput)
        val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val addButton = dialogView.findViewById<Button>(R.id.addButton)

        cancelButton.setOnClickListener { alertDialog.dismiss() }

        addButton.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val course = courseInput.text.toString().trim()
            val year = yearInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && course.isNotEmpty() && year.isNotEmpty()) {
                if (password == confirmPassword) {
                    val newStudent = Student(email, name, course, year, password)
                    studentsList.add(newStudent)
                    studentsAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "Student added successfully", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                } else {
                    confirmPasswordInput.error = "Passwords do not match"
                }
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
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
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                requireActivity().finish() // Close the current activity
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }
}
