package com.example.appque.fragments

import Staff
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appque.R
import com.example.appque.StaffAdapter
import com.example.appque.databinding.FragmentStaffBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StaffFragment : Fragment() {

    private var _binding: FragmentStaffBinding? = null
    private val binding get() = _binding!!

    private var staffAdapter: StaffAdapter? = null
    private val staffList = mutableListOf<Staff>()
    private val filteredList = mutableListOf<Staff>() // List for filtered results
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val VALID_WINDOWS = listOf(
        "window1", "window2", "window3", "window4", "window5", "window6", "window7", "window8"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView with filtered list
        staffAdapter = StaffAdapter(filteredList) { staff ->
            showStaffInfoDialog(staff)
        }
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.staffRecyclerView.adapter = staffAdapter

        // Add TextWatcher to handle search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStaff(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.addStaffButton.setOnClickListener {
            showAddStaffDialog()
        }

        fetchStaff()
    }

    private fun fetchStaff() {
        try {
            binding.progressBar.visibility = View.VISIBLE

            database.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            binding.progressBar.visibility = View.GONE
                            staffList.clear()

                            for (childSnapshot in snapshot.children) {
                                try {
                                    val staff = childSnapshot.getValue(Staff::class.java)
                                    if (staff?.role in VALID_WINDOWS) {
                                        staff?.let {
                                            it.firebaseUid = childSnapshot.key ?: ""
                                            staffList.add(it)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("StaffFragment", "Error parsing staff data: ${e.message}")
                                }
                            }

                            // Sort the staffList by timestamp in descending order
                            staffList.sortByDescending { it.timestamp }

                            // Update the filtered list and notify the adapter
                            filteredList.clear()
                            filteredList.addAll(staffList)

                            staffAdapter?.notifyDataSetChanged()

                            // Set empty list text visibility
                            binding.emptyListTextView.visibility =
                                if (staffList.isEmpty()) View.VISIBLE else View.GONE

                            // Scroll to the top
                            if (staffList.isNotEmpty()) {
                                binding.staffRecyclerView.scrollToPosition(0)
                            }
                        } catch (e: Exception) {
                            Log.e("StaffFragment", "Error processing snapshot data: ${e.message}")
                            Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressBar.visibility = View.GONE
                        Log.e("StaffFragment", "Database error: ${error.message}")
                        Toast.makeText(requireContext(), "Error fetching staff: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Log.e("StaffFragment", "Unexpected error: ${e.message}")
            Toast.makeText(requireContext(), "An unexpected error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }




    private fun filterStaff(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredList.clear()

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(staffList)
        } else {
            staffList.forEach { staff ->
                if (staff.name?.lowercase()?.contains(lowerCaseQuery) == true ||
                    staff.id?.lowercase()?.contains(lowerCaseQuery) == true ||
                    staff.email?.lowercase()?.contains(lowerCaseQuery) == true ||
                    staff.role?.lowercase()?.contains(lowerCaseQuery) == true
                ) {
                    filteredList.add(staff)
                }
            }
        }
        staffAdapter?.notifyDataSetChanged()

        binding.emptyListTextView.visibility =
            if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showAddStaffDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_staff, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val idInput = dialogView.findViewById<EditText>(R.id.idInput)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)
        val roleSpinner = dialogView.findViewById<Spinner>(R.id.roleSpinner)

        val roles = arrayOf(
            "Select Window", "window1", "window2", "window3", "window4", "window5", "window6", "window7", "window8"
        )
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = roleAdapter

        dialogView.findViewById<Button>(R.id.addButton).setOnClickListener {
            val id = idInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (validateInputs(id, name, email, password, confirmPassword, role)) {
                addStaff(id, name, email, role, password, dialog)
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addStaff(
        id: String,
        name: String,
        email: String,
        role: String,
        password: String,
        dialog: AlertDialog
    ) {
        try {
            if (role == "Select Window" || role.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a valid window.", Toast.LENGTH_SHORT).show()
                return
            }

            binding.progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { authTask ->
                    try {
                        if (authTask.isSuccessful) {
                            val userId = authTask.result?.user?.uid
                            if (userId != null) {
                                val staff = Staff(
                                    id = id,
                                    name = name,
                                    email = email,
                                    role = role,
                                    firebaseUid = userId,
                                    timestamp = System.currentTimeMillis() // Set current timestamp
                                )
                                database.child("users").child(userId).setValue(staff)
                                    .addOnSuccessListener {
                                        binding.progressBar.visibility = View.GONE
                                        dialog.dismiss()
                                        Toast.makeText(requireContext(), "Staff added successfully!", Toast.LENGTH_SHORT).show()

                                        // Add the new staff to the top of the lists
                                        staffList.add(0, staff)
                                        filteredList.add(0, staff)

                                        // Notify the adapter and scroll to the top
                                        staffAdapter?.notifyItemInserted(0)
                                        binding.staffRecyclerView.scrollToPosition(0)

                                        signOutNewUserAndRestoreAdmin()
                                    }
                                    .addOnFailureListener {
                                        binding.progressBar.visibility = View.GONE
                                        Toast.makeText(requireContext(), "Failed to add staff.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Error: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        binding.progressBar.visibility = View.GONE
                        Log.e("AddStaff", "Unexpected error: ${e.message}")
                        Toast.makeText(requireContext(), "An error occurred.", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Log.e("AddStaff", "Error adding staff: ${e.message}")
            Toast.makeText(requireContext(), "An unexpected error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    @SuppressLint("SetTextI18n")
    private fun showStaffInfoDialog(staff: Staff) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_staff_info, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val nameTextView = dialogView.findViewById<TextView>(R.id.nameText)
        val idTextView = dialogView.findViewById<TextView>(R.id.idText)
        val emailTextView = dialogView.findViewById<TextView>(R.id.emailText)
        val updateButton = dialogView.findViewById<Button>(R.id.updateButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

        nameTextView.text = "Name: ${staff.name}"
        idTextView.text = "ID: ${staff.id}"
        emailTextView.text = "Email: ${staff.email}"

        updateButton.setOnClickListener {
            dialog.dismiss()
            showUpdateStaffDialog(staff)
        }

        deleteButton.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(staff)
        }

        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteConfirmationDialog(staff: Staff) {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want to delete ${staff.name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                // Show progress bar
                binding.progressBar.visibility = View.VISIBLE

                val firebaseUid = staff.firebaseUid
                if (firebaseUid != null) {
                    if (firebaseUid.isNotEmpty()) {
                        if (firebaseUid != null) {
                            database.child("users").child(firebaseUid).removeValue()
                                .addOnSuccessListener {
                                    // Hide progress bar after success
                                    binding.progressBar.visibility = View.GONE
                                    staffList.remove(staff)
                                    filteredList.clear()
                                    filteredList.addAll(staffList)
                                    staffAdapter?.notifyDataSetChanged()
                                    Toast.makeText(requireContext(), "${staff.name} deleted successfully!", Toast.LENGTH_SHORT).show()

                                    // Restore admin session
                                    signOutNewUserAndRestoreAdmin()
                                }
                                .addOnFailureListener { exception ->
                                    // Hide progress bar after failure
                                    binding.progressBar.visibility = View.GONE
                                    Log.e("DeleteStaff", "Failed to delete: ${exception.message}")
                                    Toast.makeText(requireContext(), "Failed to delete staff: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Hide progress bar if no valid UID is found
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Invalid staff UID. Cannot delete.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun showUpdateStaffDialog(staff: Staff) {
        try {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_staff, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create()

            val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
            val roleSpinner = dialogView.findViewById<Spinner>(R.id.roleSpinner)

            nameInput.setText(staff.name)

            val roles = arrayOf("Select Window", "window1", "window2", "window3", "window4", "window5", "window6", "window7", "window8")
            val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            roleSpinner.adapter = roleAdapter
            roleSpinner.setSelection(roles.indexOf(staff.role))

            dialogView.findViewById<Button>(R.id.addButton).apply {
                text = "Update"
                setOnClickListener {
                    val updatedName = nameInput.text.toString().trim()
                    val updatedRole = roleSpinner.selectedItem.toString()

                    if (updatedName.isEmpty() || updatedRole == "Select Window") {
                        Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    binding.progressBar.visibility = View.VISIBLE

                    val updatedStaff = staff.copy(name = updatedName, role = updatedRole)

                    try {
                        staff.firebaseUid?.let { it1 ->
                            database.child("users").child(it1).updateChildren(
                                mapOf("name" to updatedName, "role" to updatedRole)
                            ).addOnSuccessListener {
                                binding.progressBar.visibility = View.GONE
                                staffList.remove(staff)
                                staffList.add(0, updatedStaff)
                                filteredList.clear()
                                filteredList.addAll(staffList)
                                staffAdapter?.notifyDataSetChanged()
                                Toast.makeText(requireContext(), "${staff.name} updated successfully!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }.addOnFailureListener { exception ->
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), "Failed to update: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        binding.progressBar.visibility = View.GONE
                        Log.e("UpdateStaff", "Error updating staff: ${e.message}")
                        Toast.makeText(requireContext(), "An error occurred.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("UpdateStaff", "Error displaying update dialog: ${e.message}")
            Toast.makeText(requireContext(), "An error occurred while showing the dialog.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun signOutNewUserAndRestoreAdmin() {
        val adminEmail = "admin@gmail.com" // Replace with your admin's email
        val adminPassword = "123456" // Replace with your admin's password

        auth.signOut()

        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Admin session restored.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to restore admin session: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInputs(
        id: String,
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String
    ): Boolean {
        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || role == "Select Window") {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}