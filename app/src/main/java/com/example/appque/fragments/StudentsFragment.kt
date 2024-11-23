package com.example.appque.fragments

import Student
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
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

    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var studentsAdapter: StudentsAdapter
    private val studentsList = mutableListOf<Student>()
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentsAdapter = StudentsAdapter(studentsList) { student ->
            showStudentInfoDialog(student)
        }
        binding.studentsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.studentsRecyclerView.adapter = studentsAdapter

        binding.addStudentButton.setOnClickListener {
            showAddStudentDialog()
        }

        fetchStudents()
    }

    private fun fetchStudents() {
        binding.progressBar.visibility = View.VISIBLE

        database.child("users").orderByChild("role").equalTo("student")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    studentsList.clear()
                    for (childSnapshot in snapshot.children) {
                        val student = childSnapshot.getValue(Student::class.java)
                        if (student != null) {
                            student.id = student.id // Ensure custom ID is used
                            studentsList.add(student)
                        }
                    }
                    studentsAdapter.notifyDataSetChanged()
                    binding.emptyListTextView.visibility =
                        if (studentsList.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error fetching students", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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
                addStudent(id, name, email, course, year, password, dialog)
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showStudentInfoDialog(student: Student) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_student_info, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val idText = dialogView.findViewById<TextView>(R.id.idText)
        val nameText = dialogView.findViewById<TextView>(R.id.nameText)
        val courseText = dialogView.findViewById<TextView>(R.id.courseText)
        val yearText = dialogView.findViewById<TextView>(R.id.yearText)
        val updateButton = dialogView.findViewById<Button>(R.id.updateButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

        idText.text = student.id
        nameText.text = student.name
        courseText.text = student.course
        yearText.text = student.year

        updateButton.setOnClickListener {
            dialog.dismiss()
            showUpdateStudentDialog(student)
        }

        deleteButton.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(student)
        }

        dialog.show()
    }

    private fun showUpdateStudentDialog(student: Student) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_student, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val idInput = dialogView.findViewById<EditText>(R.id.idInput)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val courseSpinner = dialogView.findViewById<Spinner>(R.id.courseSpinner)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.yearSpinner)

        idInput.setText(student.id)
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
                val updatedStudent = student.copy(
                    id = idInput.text.toString().trim(),
                    name = nameInput.text.toString().trim(),
                    course = courseSpinner.selectedItem.toString(),
                    year = yearSpinner.selectedItem.toString()
                )

                database.child("users").orderByChild("id").equalTo(student.id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (childSnapshot in snapshot.children) {
                                childSnapshot.ref.setValue(updatedStudent)
                                    .addOnSuccessListener {
                                        dialog.dismiss()
                                        Toast.makeText(requireContext(), "Student updated successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Failed to update student.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), "Error updating student.", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want to delete ${student.name}?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                database.child("users").orderByChild("id").equalTo(student.id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (childSnapshot in snapshot.children) {
                                // Remove student from Firebase
                                childSnapshot.ref.removeValue()
                                    .addOnSuccessListener {
                                        // Safely remove the student from the list
                                        studentsList.remove(student)
                                        studentsAdapter.notifyDataSetChanged()
                                        Toast.makeText(
                                            requireContext(),
                                            "Student deleted successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Check if list is empty and update UI
                                        if (studentsList.isEmpty()) {
                                            binding.emptyListTextView.visibility = View.VISIBLE
                                            binding.studentsRecyclerView.visibility = View.GONE
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to delete student.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), "Error deleting student.", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }


    private fun setupYearSpinner(yearSpinner: Spinner, selectedCourse: String) {
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

    private fun addStudent(
        id: String,
        name: String,
        email: String,
        course: String,
        year: String,
        password: String,
        dialog: AlertDialog
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val userId = authTask.result?.user?.uid
                    if (userId != null) {
                        val student = Student(id, name, email, course, year, "student")
                        database.child("users").child(userId).setValue(student)
                            .addOnSuccessListener {
                                dialog.dismiss()
                                Toast.makeText(requireContext(), "Student added successfully!", Toast.LENGTH_SHORT).show()
                                fetchStudents()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to add student.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
