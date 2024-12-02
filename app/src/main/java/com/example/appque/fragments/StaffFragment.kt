package com.example.appque.fragments

import Staff
import android.app.AlertDialog
import android.os.Bundle
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

    private lateinit var staffAdapter: StaffAdapter
    private val staffList = mutableListOf<Staff>()
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val VALID_ROLES = listOf(
        "staff1", "staff2", "staff3", "staff4", "staff5", "staff6", "staff7", "staff8"
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

        staffAdapter = StaffAdapter(staffList) { staff ->
            showStaffInfoDialog(staff)
        }

        binding.staffRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.staffRecyclerView.adapter = staffAdapter

        binding.addStaffButton.setOnClickListener {
            showAddStaffDialog()
        }

        fetchStaff()
    }

    private fun fetchStaff() {
        binding.progressBar.visibility = View.VISIBLE

        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    staffList.clear()
                    for (childSnapshot in snapshot.children) {
                        val staff = childSnapshot.getValue(Staff::class.java)
                        if (staff?.role in VALID_ROLES) {
                            staff?.let {
                                it.firebaseUid = childSnapshot.key ?: "" // Attach Firebase UID
                                staffList.add(0, it) // Add to top of the list
                            }
                        }
                    }
                    staffAdapter.notifyDataSetChanged()
                    binding.emptyListTextView.visibility =
                        if (staffList.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error fetching staff: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
            "Select Role", "staff1", "staff2", "staff3", "staff4", "staff5", "staff6", "staff7", "staff8"
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

    private fun addStaff(id: String, name: String, email: String, role: String, password: String, dialog: AlertDialog) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val userId = authTask.result?.user?.uid
                    if (userId != null) {
                        val staff = Staff(id, name, email, role).apply { firebaseUid = userId }
                        database.child("users").child(userId).setValue(staff)
                            .addOnSuccessListener {
                                dialog.dismiss()
                                Toast.makeText(requireContext(), "Staff added successfully!", Toast.LENGTH_SHORT).show()
                                staffList.add(0, staff)
                                staffAdapter.notifyItemInserted(0)
                                binding.staffRecyclerView.scrollToPosition(0)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to add staff.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

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

    private fun showUpdateStaffDialog(staff: Staff) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_staff, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val idInput = dialogView.findViewById<EditText>(R.id.idInput)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val roleSpinner = dialogView.findViewById<Spinner>(R.id.roleSpinner)

        idInput.setText(staff.id)
        idInput.isEnabled = false // Make the ID field non-editable
        nameInput.setText(staff.name)
        emailInput.setText(staff.email)
        emailInput.isEnabled = false // Disallow editing the email

        val roles = arrayOf("Select Role", "staff1", "staff2", "staff3", "staff4", "staff5", "staff6", "staff7", "staff8")
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = roleAdapter
        roleSpinner.setSelection(roles.indexOf(staff.role))

        dialogView.findViewById<Button>(R.id.addButton).apply {
            text = "Update"
            setOnClickListener {
                val updatedName = nameInput.text.toString().trim()
                val updatedRole = roleSpinner.selectedItem.toString()

                if (updatedName.isEmpty() || updatedRole == "Select Role") {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedStaff = staff.copy(name = updatedName, role = updatedRole)

                database.child("users").child(staff.firebaseUid).updateChildren(
                    mapOf(
                        "name" to updatedName,
                        "role" to updatedRole
                    )
                ).addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "${staff.name} updated successfully!", Toast.LENGTH_SHORT).show()

                    staffList.remove(staff)
                    staffList.add(0, updatedStaff)
                    staffAdapter.notifyDataSetChanged()
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to update: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(staff: Staff) {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want to delete ${staff.name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val firebaseUid = staff.firebaseUid
                if (firebaseUid.isNotEmpty()) {
                    database.child("users").child(firebaseUid).removeValue()
                        .addOnSuccessListener {
                            staffList.remove(staff)
                            staffAdapter.notifyDataSetChanged()
                            Toast.makeText(requireContext(), "${staff.name} deleted successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DeleteStaff", "Failed to delete: ${exception.message}")
                            Toast.makeText(requireContext(), "Failed to delete staff: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Invalid staff UID. Cannot delete.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun validateInputs(
        id: String,
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String
    ): Boolean {
        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || role == "Select Role") {
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
