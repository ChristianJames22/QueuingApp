package com.example.appque

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appque.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize View Binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjust for edge-to-edge layout using View Binding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.main.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // Set up spinners for Course and Year
        setupCourseSpinner()
        setupYearSpinner()

        // Set click listener for the "Continue" button
        binding.continueButton.setOnClickListener {
            handleSignUp()
        }
    }

    private fun setupCourseSpinner() {
        val courses = resources.getStringArray(R.array.course_options) // Example: ["Select Course", "BSIT", "HM", "BEED", "SHS"]
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseSpinner.adapter = adapter

        binding.courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateYearOptions(courses[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupYearSpinner() {
        updateYearOptions("Select Course") // Default year options
    }

    private fun updateYearOptions(selectedCourse: String) {
        val years = when (selectedCourse) {
            "SHS" -> arrayOf("Select Year", "G-11", "G-12")
            else -> arrayOf("Select Year", "1st", "2nd", "3rd", "4th")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.yearSpinner.adapter = adapter
    }

    private fun handleSignUp() {
        val id = binding.idInput.text.toString().trim()
        val name = binding.nameInput.text.toString().trim()
        val course = binding.courseSpinner.selectedItem?.toString()?.trim() ?: ""
        val year = binding.yearSpinner.selectedItem?.toString()?.trim() ?: ""
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

        if (validateInputs(id, name, course, year, email, password, confirmPassword)) {
            registerUser(email, password, id, name, course, year)
        }
    }

    private fun validateInputs(
        id: String,
        name: String,
        course: String,
        year: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            id.isEmpty() || name.isEmpty() || course == "Select Course" || year == "Select Year" || email.isEmpty() || password.isEmpty() -> {
                showToast("Please fill in all the fields")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Invalid email format")
                false
            }
            password.length < 6 -> {
                showToast("Password must be at least 6 characters")
                false
            }
            password != confirmPassword -> {
                showToast("Passwords do not match")
                false
            }
            else -> true
        }
    }

    private fun registerUser(email: String, password: String, id: String, name: String, course: String, year: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, id, name, course, year, email)
                    } else {
                        showToast("User registration failed")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    showToast(errorMessage)
                    Log.e("SignUpActivity", "Error: $errorMessage")
                }
            }
    }

    private fun saveUserToFirestore(userId: String, id: String, name: String, course: String, year: String, email: String) {
        val user = mapOf(
            "id" to id,
            "name" to name,
            "course" to course,
            "year" to year,
            "email" to email,
            "role" to "student" // Default role
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                showToast("Student registered successfully")
                finish() // Navigate back to the login screen
            }
            .addOnFailureListener { e ->
                showToast("Failed to save user details")
                Log.e("SignUpActivity", "Error saving user to Firestore: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
