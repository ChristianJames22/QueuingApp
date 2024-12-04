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

class StudentsFragment : Fragment() {

    // View binding for the fragment
    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    // Adapter and lists for managing students
    private lateinit var studentsAdapter: StudentsAdapter
    private val studentsList = mutableListOf<Student>() // List of all students
    private val filteredList = mutableListOf<Student>() // List for filtered results

    // Firebase references
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private var valueEventListener: ValueEventListener? = null // Listener for database updates

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView with the adapter
        studentsAdapter = StudentsAdapter(filteredList) { student ->
            showStudentInfoDialog(student) // Show dialog for the selected student
        }
        binding.studentsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.studentsRecyclerView.adapter = studentsAdapter

        // Add TextWatcher for search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStudents(s.toString()) // Filter students as the user types
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle "Add Student" button click
        binding.addStudentButton.setOnClickListener {
            showAddStudentDialog() // Show dialog to add a new student
        }

        // Fetch students from Firebase
        fetchStudents()
    }

    // Fetch students from Firebase Realtime Database
    private fun fetchStudents() {
        binding.progressBar.visibility = View.VISIBLE

        valueEventListener = database.child("users").orderByChild("role").equalTo("student")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE

                    val newStudentsList = mutableListOf<Student>()
                    for (childSnapshot in snapshot.children) {
                        val student = childSnapshot.getValue(Student::class.java)
                        if (student != null) {
                            student.id = childSnapshot.child("id").value?.toString() ?: "Unknown ID"
                            newStudentsList.add(student)
                        }
                    }

                    if (newStudentsList != studentsList) {
                        studentsList.clear()
                        studentsList.addAll(newStudentsList.reversed())
                        filteredList.clear()
                        filteredList.addAll(studentsList)
                        studentsAdapter.notifyDataSetChanged()
                    }

                    binding.emptyListTextView.visibility =
                        if (studentsList.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Log.e("StudentsFragment", "Error fetching students: ${error.message}")
                    Toast.makeText(requireContext(), "Error fetching students.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Filter students based on the search query
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

    // Show detailed info dialog for a student
    private fun showStudentInfoDialog(student: Student) {
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

        // Handle update action
        updateButton.setOnClickListener {
            dialog.dismiss()
            showUpdateStudentDialog(student)
        }

        // Handle delete action
        deleteButton.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(student)
        }

        dialog.show()
    }

    // Show dialog to update a student
    private fun showUpdateStudentDialog(student: Student) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_student, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val idInput = dialogView.findViewById<EditText>(R.id.idInput)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val courseSpinner = dialogView.findViewById<Spinner>(R.id.courseSpinner)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.yearSpinner)

        // Populate current student information
        idInput.setText(student.id)
        idInput.isEnabled = false
        emailInput.setText(student.email)
        emailInput.isEnabled = false
        nameInput.setText(student.name)

        val courses = resources.getStringArray(R.array.course_options)
        val courseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter
        courseSpinner.setSelection(courses.indexOf(student.course))

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

        dialogView.findViewById<Button>(R.id.addButton).apply {
            text = "Update"
            setOnClickListener {
                val updatedName = nameInput.text.toString().trim()
                val updatedCourse = courseSpinner.selectedItem.toString()
                val updatedYear = yearSpinner.selectedItem.toString()

                if (updatedName.isEmpty() || updatedCourse == "Select Course" || updatedYear == "Select Year") {
                    Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update student object locally
                val updatedStudent = student.copy(
                    name = updatedName,
                    course = updatedCourse,
                    year = updatedYear
                )

                // Update the student in the Firebase database
                database.child("users").orderByChild("id").equalTo(student.id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (childSnapshot in snapshot.children) {
                                    childSnapshot.ref.setValue(updatedStudent)
                                        .addOnSuccessListener {
                                            // Update the local list
                                            val index = studentsList.indexOf(student)
                                            if (index != -1) {
                                                studentsList[index] = updatedStudent
                                                filteredList[index] = updatedStudent
                                                studentsAdapter.notifyItemChanged(index)
                                            }
                                            Toast.makeText(requireContext(), "Student updated successfully!", Toast.LENGTH_SHORT).show()
                                            dialog.dismiss()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(requireContext(), "Failed to update student: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(requireContext(), "Student not found in database.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), "Error updating student: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    // Show delete confirmation dialog
    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want to delete ${student.name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                database.child("users").orderByChild("id").equalTo(student.id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (childSnapshot in snapshot.children) {
                                    childSnapshot.ref.removeValue()
                                        .addOnSuccessListener {
                                            studentsList.remove(student)
                                            filteredList.clear()
                                            filteredList.addAll(studentsList)
                                            studentsAdapter.notifyDataSetChanged()
                                            Toast.makeText(requireContext(), "Student deleted successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(requireContext(), "Failed to delete student: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(requireContext(), "Student not found in database.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), "Error deleting student: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Show add student dialog
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
                addStudentToList(id, name, email, course, year, dialog)
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addStudentToList(
        id: String,
        name: String,
        email: String,
        course: String,
        year: String,
        dialog: AlertDialog
    ) {
        val newStudent = Student(id, name, email, course, year, "student")
        studentsList.add(0, newStudent)
        filteredList.add(0, newStudent)
        studentsAdapter.notifyItemInserted(0)
        binding.studentsRecyclerView.scrollToPosition(0)

        Toast.makeText(requireContext(), "Student added successfully!", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    // Set up year spinner based on selected course
    private fun setupYearSpinner(yearSpinner: Spinner, selectedCourse: String) {
        val years = when (selectedCourse) {
            "SHS" -> arrayOf("Select Year", "G-11", "G-12")
            else -> arrayOf("Select Year", "1st", "2nd", "3rd", "4th")
        }

        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter
    }

    // Validate input fields for adding/updating students
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
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(requireContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (valueEventListener != null) {
            database.child("users").removeEventListener(valueEventListener!!)
        }
        _binding = null
    }
}
