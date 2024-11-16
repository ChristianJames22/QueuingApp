package com.example.appque.fragments

import Staff // Ensure this is the correct model class
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
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
import com.example.appque.MainActivity
import com.example.appque.R
import com.example.appque.StaffAdapter

class StaffFragment : Fragment() {

    private lateinit var staffAdapter: StaffAdapter
    private val staffList = mutableListOf<Staff>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff, container, false)

        // Setup RecyclerView and Adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.staffRecyclerView)
        staffAdapter = StaffAdapter(staffList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = staffAdapter

        // Add button click listener
        val addStaffButton: ImageButton = view.findViewById(R.id.addStaffButton)
        addStaffButton.setOnClickListener {
            showAddStaffDialog()
        }

        // Settings button click listener
        val settingsButton: ImageButton = view.findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showSettingsMenu(settingsButton)
        }

        return view
    }

    private fun showAddStaffDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_staff, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog = dialogBuilder.create()

        val idInput = dialogView.findViewById<EditText>(R.id.idInput)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val addButton = dialogView.findViewById<Button>(R.id.addButton)

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        addButton.setOnClickListener {
            val id = idInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (id.isNotEmpty() && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (password == confirmPassword) {
                    val newStaff = Staff(id, name, email)
                    staffList.add(newStaff)
                    staffAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Staff added successfully", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                } else {
                    confirmPasswordInput.error = "Passwords do not match"
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }
}
