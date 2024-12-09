

package com.example.appque.fragments

import Student
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
import com.example.appque.StudentsAdapter
import com.example.appque.databinding.FragmentStudentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentsFragment : Fragment() {

    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var studentsAdapter: StudentsAdapter
    private val studentsList = mutableListOf<Student>()
    private val filteredList = mutableListOf<Student>()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var secondaryAuth: FirebaseAuth
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize primary Firebase instance
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

    }

    private fun signOutNewUserAndRestoreAdmin() {
        val adminEmail = "admin@gmail.com" // Replace with your admin's email
        val adminPassword = "123456" // Replace with your admin's password

        auth.signOut() // Sign out the currently logged-in user

        // Log back in as the admin
        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Admin session restored.", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed to restore admin session: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)

            // Set up RecyclerView adapter and layout manager
            studentsAdapter = StudentsAdapter(filteredList) { student ->
                showStudentInfoDialog(student)
            }
            binding.studentsRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.studentsRecyclerView.adapter = studentsAdapter

            // Search functionality
            binding.searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filterStudents(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Add student button
            binding.addStudentButton.setOnClickListener {
                showAddStudentDialog()
            }

            // Fetch students from the database
            fetchStudents()
        } catch (e: Exception) {
            Log.e("StudentsFragment", "Unexpected error in onViewCreated: ${e.message}")
            Toast.makeText(requireContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchStudents() {
        try {
            binding.progressBar.visibility = View.VISIBLE
            valueEventListener = database.child("users").orderByChild("role").equalTo("student")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            binding.progressBar.visibility = View.GONE
                            val newStudentsList = mutableListOf<Student>()

                            if (snapshot.exists()) {
                                Log.d(
                                    "StudentsFragment",
                                    "Fetched Data: ${snapshot.value}"
                                ) // Log entire snapshot
                                for (childSnapshot in snapshot.children) {
                                    try {
                                        val student =
                                            childSnapshot.getValue(Student::class.java)?.apply {
                                                uid = childSnapshot.key ?: "Unknown UID"
                                            }
                                        if (student != null) {
                                            newStudentsList.add(student)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            "StudentsFragment",
                                            "Error parsing student data: ${e.message}"
                                        )
                                    }
                                }
                            } else {
                                Log.w(
                                    "StudentsFragment",
                                    "No data found in 'users' with role = 'student'"
                                )
                            }

                            studentsList.clear()
                            studentsList.addAll(newStudentsList.sortedByDescending { it.timestamp })
                            filteredList.clear()
                            filteredList.addAll(studentsList)
                            studentsAdapter.notifyDataSetChanged()

                            binding.emptyListTextView.visibility =
                                if (studentsList.isEmpty()) View.VISIBLE else View.GONE

                            updateRecentlyAdded()
                            updateStudentCounts()
                        } catch (e: Exception) {
                            Log.e(
                                "StudentsFragment",
                                "Error processing snapshot data: ${e.message}"
                            )
                            Toast.makeText(
                                requireContext(),
                                "An error occurred: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressBar.visibility = View.GONE
                        Log.e("StudentsFragment", "Database error: ${error.message}")
                        Toast.makeText(
                            requireContext(),
                            "Error fetching students: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Log.e("StudentsFragment", "Unexpected error: ${e.message}")
            Toast.makeText(
                requireContext(),
                "An unexpected error occurred: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun updateRecentlyAdded() {
        val recentlyAdded = studentsList
            .take(5) // Get the first 5 students (already sorted by timestamp)
            .joinToString("\n") {
                "${it.name.ifEmpty { "Unknown Name" }} - ${it.course.ifEmpty { "Unknown Course" }} ${it.year.ifEmpty { "Unknown Year Level" }}"
            }

        binding.recentlyAddedList.text = if (recentlyAdded.isNotEmpty()) {
            recentlyAdded
        } else {
            "No recent students."
        }
    }

    private fun updateStudentCounts() {
        val courseCounts = mutableMapOf(
            "SHS" to 0,
            "BSHM" to 0,
            "BSIT" to 0,
            "BEED" to 0,
            "BSBA" to 0
        )
        val levelCounts = mutableMapOf(
            "G-12" to 0,
            "G-11" to 0,
            "1st" to 0,
            "2nd" to 0,
            "3rd" to 0,
            "4th" to 0
        )
        var totalStudents = 0

        for (student in studentsList) {
            courseCounts[student.course] = courseCounts.getOrDefault(student.course, 0) + 1
            levelCounts[student.year.replace(" Year", "")] = levelCounts.getOrDefault(
                student.year.replace(" Year", ""),
                0
            ) + 1
            totalStudents++
        }

        val studentCountsText = courseCounts.entries.joinToString("\n") { "${it.key}: ${it.value}" }
            .plus("\nTotal: $totalStudents")
        binding.studentCountsTextView.text = studentCountsText

        val levelCountsText = levelCounts.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        binding.levelCountsTextView.text = levelCountsText
    }

    private fun filterStudents(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredList.clear()
        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(studentsList)
        } else {
            studentsList.forEach { student ->
                if (student.name.lowercase().contains(lowerCaseQuery) ||
                    student.id.lowercase().contains(lowerCaseQuery) ||
                    student.email.lowercase().contains(lowerCaseQuery) ||
                    student.course.lowercase().contains(lowerCaseQuery) ||
                    student.year.lowercase().contains(lowerCaseQuery)
                ) {
                    filteredList.add(student)
                }
            }
        }

        studentsAdapter.notifyDataSetChanged()
        binding.emptyListTextView.visibility =
            if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showStudentInfoDialog(student: Student) {
        try {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_student_info, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create()

            val idTextView = dialogView.findViewById<TextView>(R.id.idText)
            val nameTextView = dialogView.findViewById<TextView>(R.id.nameText)
            val courseTextView = dialogView.findViewById<TextView>(R.id.courseText)
            val yearTextView = dialogView.findViewById<TextView>(R.id.yearText)
            val updateButton = dialogView.findViewById<Button>(R.id.updateButton)
            val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

            idTextView.text = "ID: ${student.id}"
            nameTextView.text = "Name: ${student.name}"
            courseTextView.text = "Course: ${student.course}"
            yearTextView.text = "Year: ${student.year}"

            updateButton.setOnClickListener {
                dialog.dismiss()
                showUpdateStudentDialog(student)
            }

            deleteButton.setOnClickListener {
                dialog.dismiss()
                showDeleteConfirmationDialog(student)
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("ShowStudentDialog", "Error displaying student info dialog: ${e.message}")
            Toast.makeText(requireContext(), "An error occurred while displaying the dialog.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showUpdateStudentDialog(student: Student) {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_student, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Bind UI elements to the dialog
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val courseSpinner = dialogView.findViewById<Spinner>(R.id.courseSpinner)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.yearSpinner)

        // Populate the fields with existing student data
        nameInput.setText(student.name)

        // Set up course spinner
        val courses = resources.getStringArray(R.array.course_options)
        val courseAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter
        courseSpinner.setSelection(courses.indexOf(student.course))

        // Set up year spinner based on initial course selection
        setupYearSpinner(yearSpinner, student.course)

        yearSpinner.setSelection(
            when (student.year) {
                "G-11" -> 1
                "G-12" -> 2
                "1st" -> 1
                "2nd" -> 2
                "3rd" -> 3
                "4th" -> 4
                else -> 0
            }
        )

        // Handle course spinner selection changes
        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCourse = courses[position]
                setupYearSpinner(yearSpinner, selectedCourse)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Update button logic
        dialogView.findViewById<Button>(R.id.addButton).setOnClickListener {
            val updatedName = nameInput.text.toString().trim()
            val updatedCourse = courseSpinner.selectedItem.toString()
            val updatedYear = yearSpinner.selectedItem.toString()

            // Validate input fields
            if (updatedName.isEmpty() || updatedCourse == "Select Course" || updatedYear == "Select Year") {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Create updated student object
            val updatedStudent = student.copy(
                name = updatedName,
                course = updatedCourse,
                year = updatedYear
            )

            // Show loading indicator
            showLoading()

            // Update student in the database
            database.child("users").child(student.uid).setValue(updatedStudent)
                .addOnSuccessListener {
                    studentsList.removeAll { it.uid == student.uid }
                    filteredList.removeAll { it.uid == student.uid }

                    studentsList.add(0, updatedStudent)
                    filteredList.add(0, updatedStudent)

                    studentsAdapter.notifyDataSetChanged()
                    binding.studentsRecyclerView.scrollToPosition(0)

                    // Hide loading indicator
                    hideLoading()

                    Toast.makeText(
                        requireContext(),
                        "Student updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { exception ->
                    // Hide loading indicator
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "Failed to update student: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Cancel button logic
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want to delete ${student.name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                showLoading()
                database.child("users").child(student.uid).removeValue()
                    .addOnSuccessListener {
                        studentsList.remove(student)
                        filteredList.clear()
                        filteredList.addAll(studentsList)
                        studentsAdapter.notifyDataSetChanged()

                        // Restore admin session
                        signOutNewUserAndRestoreAdmin()

                        hideLoading()
                        Toast.makeText(
                            requireContext(),
                            "Student deleted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { exception ->
                        hideLoading()
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete student: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showAddStudentDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_student, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val idInput = dialogView.findViewById<EditText>(R.id.idInput)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)
        val courseSpinner = dialogView.findViewById<Spinner>(R.id.courseSpinner)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.yearSpinner)

        val courses = resources.getStringArray(R.array.course_options)
        val courseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter

        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCourse = courses[position]
                setupYearSpinner(yearSpinner, selectedCourse)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupYearSpinner(yearSpinner, courses[0])

        dialogView.findViewById<Button>(R.id.addButton).setOnClickListener {
            val id = idInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val course = courseSpinner.selectedItem?.toString()?.trim() ?: ""
            val year = yearSpinner.selectedItem?.toString()?.trim() ?: ""
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validateInputs(id, name, email, course, year, password, confirmPassword)) {
                // Check if the student already exists by name, ID, or email
                database.child("users")
                    .orderByChild("name").equalTo(name).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(nameSnapshot: DataSnapshot) {
                            if (nameSnapshot.exists()) {
                                Toast.makeText(requireContext(), "Name already exists.", Toast.LENGTH_SHORT).show()
                                return
                            }
                            database.child("users")
                                .orderByChild("id").equalTo(id).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(idSnapshot: DataSnapshot) {
                                        if (idSnapshot.exists()) {
                                            Toast.makeText(requireContext(), "ID already exists.", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        database.child("users")
                                            .orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(emailSnapshot: DataSnapshot) {
                                                    if (emailSnapshot.exists()) {
                                                        Toast.makeText(requireContext(), "Email already exists.", Toast.LENGTH_SHORT).show()
                                                        return
                                                    }
                                                    // Proceed to add the student if no conflicts
                                                    addStudent(id, name, email, course, year, password, dialog)
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun addStudent(
        id: String,
        name: String,
        email: String,
        course: String,
        year: String,
        password: String,
        dialog: AlertDialog
    ) {
        showLoading()
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid ?: throw Exception("User ID is null")
                            val timestamp = System.currentTimeMillis()
                            val formattedDate =     SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                                Date(timestamp)
                            )

                            val newStudent = Student(
                                id = id,
                                name = name,
                                email = email,
                                course = course,
                                year = year,
                                timestamp = timestamp,
                                formattedDate = formattedDate, // Add formatted date
                                uid = userId,
                                role = "student"
                            )

                            database.child("users").child(userId).setValue(newStudent)
                                .addOnSuccessListener {
                                    try {
                                        studentsList.add(0, newStudent)
                                        filteredList.add(0, newStudent)
                                        studentsAdapter.notifyDataSetChanged()
                                        binding.studentsRecyclerView.scrollToPosition(0)

                                        signOutNewUserAndRestoreAdmin()
                                        hideLoading()
                                        Toast.makeText(
                                            requireContext(),
                                            "Student added successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                    } catch (e: Exception) {
                                        Log.e("AddStudent", "Error updating UI after addition: ${e.message}")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    hideLoading()
                                    Log.e("AddStudent", "Database error: ${exception.message}")
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to save student: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            hideLoading()
                            Log.e("AddStudent", "Auth error: ${task.exception?.message}")
                            Toast.makeText(
                                requireContext(),
                                "Failed to create account: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e("AddStudent", "Unexpected error: ${e.message}")
                        hideLoading()
                        Toast.makeText(requireContext(), "An error occurred.", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("AddStudent", "Error in addStudent: ${e.message}")
            hideLoading()
            Toast.makeText(requireContext(), "An unexpected error occurred.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun setupYearSpinner(yearSpinner: Spinner, selectedCourse: String) {
        val years = when (selectedCourse) {
            "SHS" -> arrayOf("Select Year", "G-11", "G-12")
            else -> arrayOf("Select Year", "1st", "2nd", "3rd", "4th")
        }
        val yearAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter
    }

    private fun validateInputs(
        id: String,
        name: String,
        email: String,
        course: String,
        year: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            id.isEmpty() || name.isEmpty() || email.isEmpty() || course == "Select Course" || year == "Select Year" -> {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            password != confirmPassword -> {
                Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            password.length < 6 -> {
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 6 characters.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            else -> true
        }
    }

    private fun showLoading() {
        binding.loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        try {
            super.onDestroyView()
            if (valueEventListener != null) {
                database.child("users").removeEventListener(valueEventListener!!)
            }
            _binding = null
        } catch (e: Exception) {
            Log.e("StudentsFragment", "Error in onDestroyView: ${e.message}")
        }
    }
}

