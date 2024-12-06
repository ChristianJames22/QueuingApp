package com.example.appque.fragments

import Student
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appque.R
import com.example.appque.StudentsAdapter
import com.example.appque.databinding.FragmentStudentsBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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

        // Initialize secondary Firebase instance safely
        initializeSecondaryAuth()
    }

    private fun initializeSecondaryAuth() {
        try {
            val secondaryApp = FirebaseApp.getInstance("SecondaryApp")
            secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
        } catch (e: IllegalStateException) {
            val secondaryApp = FirebaseApp.initializeApp(
                requireContext(),
                FirebaseApp.getInstance().options,
                "SecondaryApp"
            )
            secondaryAuth = FirebaseAuth.getInstance(secondaryApp!!)
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
    }

    private fun fetchStudents() {
        binding.progressBar.visibility = View.VISIBLE

        // Set up the ValueEventListener
        valueEventListener = database.child("users").orderByChild("role").equalTo("student")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    val newStudentsList = mutableListOf<Student>()
                    for (childSnapshot in snapshot.children) {
                        val student = childSnapshot.getValue(Student::class.java)
                        if (student != null) {
                            student.uid = childSnapshot.key ?: "Unknown UID"
                            newStudentsList.add(student)
                        }
                    }
                    studentsList.clear()
                    studentsList.addAll(newStudentsList.reversed())
                    filteredList.clear()
                    filteredList.addAll(studentsList)
                    studentsAdapter.notifyDataSetChanged()

                    binding.emptyListTextView.visibility =
                        if (studentsList.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error fetching students.", Toast.LENGTH_SHORT).show()
                }
            })
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
    }

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

                val updatedStudent = student.copy(
                    name = updatedName,
                    course = updatedCourse,
                    year = updatedYear
                )

                database.child("users").child(student.uid).setValue(updatedStudent)
                    .addOnSuccessListener {
                        studentsList.removeAll { it.uid == student.uid }
                        filteredList.removeAll { it.uid == student.uid }

                        studentsList.add(0, updatedStudent)
                        filteredList.add(0, updatedStudent)

                        studentsAdapter.notifyDataSetChanged()
                        binding.studentsRecyclerView.scrollToPosition(0)

                        Toast.makeText(requireContext(), "Student updated successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Failed to update student: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
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
                database.child("users").child(student.uid).removeValue()
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
                secondaryAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                            val newStudent = Student(id, name, email, course, year, "student")

                            database.child("users").child(userId).setValue(newStudent)
                                .addOnSuccessListener {
                                    studentsList.add(0, newStudent)
                                    filteredList.add(0, newStudent)
                                    studentsAdapter.notifyDataSetChanged()
                                    binding.studentsRecyclerView.scrollToPosition(0)
                                    secondaryAuth.signOut()
                                    Toast.makeText(requireContext(), "Student added successfully!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(requireContext(), "Failed to save student: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(requireContext(), "Failed to create account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
