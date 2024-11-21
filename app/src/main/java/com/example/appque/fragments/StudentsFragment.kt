package com.example.appque.fragments

import Student
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
import com.example.appque.StudentsAdapter
import com.example.appque.databinding.FragmentStudentsBinding
import com.google.firebase.firestore.FirebaseFirestore

class StudentsFragment : Fragment() {

    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var studentsAdapter: StudentsAdapter
    private val studentsList = mutableListOf<Student>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView and Adapter
        studentsAdapter = StudentsAdapter(studentsList)
        binding.studentsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.studentsRecyclerView.adapter = studentsAdapter

        // Add Student button click listener
        binding.addStudentButton.setOnClickListener {
            showAddStudentDialog()
        }

        // Fetch the initial list of students
        fetchStudents()
    }

    private fun fetchStudents() {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("users")
            .whereEqualTo("role", "student")
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    Log.e("Firestore", "Error fetching students: ${error.message}")
                    Toast.makeText(requireContext(), "Error fetching students: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    studentsList.clear()
                    Log.d("Firestore", "Fetched students: ${snapshot.documents.size} documents")
                    it.documents.forEach { doc ->
                        val student = doc.toObject(Student::class.java)
                        if (student != null) {
                            studentsList.add(student)
                        } else {
                            Log.e("Firestore", "Error mapping document to Student: ${doc.id}")
                        }
                    }

                    studentsAdapter.notifyDataSetChanged()

                    if (studentsList.isEmpty()) {
                        binding.emptyListTextView.visibility = View.VISIBLE
                        binding.studentsRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyListTextView.visibility = View.GONE
                        binding.studentsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun showAddStudentDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_student, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val courseSpinner = dialogView.findViewById<Spinner>(R.id.courseSpinner)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.yearSpinner)
        val idInput = dialogView.findViewById<EditText>(R.id.idInput)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)

        setupCourseSpinner(courseSpinner, yearSpinner)

        dialogView.findViewById<Button>(R.id.addButton).setOnClickListener {
            val id = idInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val course = courseSpinner.selectedItem?.toString()?.trim() ?: ""
            val year = yearSpinner.selectedItem?.toString()?.trim() ?: ""
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validateInputs(id, name, email, course, year, password, confirmPassword)) {
                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setMessage("Are you sure you want to add this student?")
                    .setPositiveButton("Yes") { _, _ ->
                        saveStudentToFirestore(id, name, email, course, year, dialog)
                    }
                    .setNegativeButton("No") { confirmationDialog, _ ->
                        confirmationDialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupCourseSpinner(courseSpinner: Spinner, yearSpinner: Spinner) {
        val courses = resources.getStringArray(R.array.course_options)
        val courseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter

        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateYearOptions(courses[position], yearSpinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateYearOptions(selectedCourse: String, yearSpinner: Spinner) {
        val years = when (selectedCourse) {
            "SHS" -> arrayOf("Select Year", "G-11", "G-12")
            else -> arrayOf("Select Year", "1st", "2nd", "3rd", "4th")
        }

        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
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
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveStudentToFirestore(
        id: String,
        name: String,
        email: String,
        course: String,
        year: String,
        dialog: AlertDialog
    ) {
        val student = mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "course" to course,
            "year" to year,
            "role" to "student"
        )

        firestore.collection("users")
            .add(student)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Student added successfully!", Toast.LENGTH_SHORT).show()
                fetchStudents()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to add student: ${exception.message}")
                Toast.makeText(requireContext(), "Failed to add student: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
