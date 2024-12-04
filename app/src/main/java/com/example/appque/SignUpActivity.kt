package com.example.appque

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appque.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    // Declare global variables
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth and FirebaseDatabase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Inflate the activity layout and set it as the content view
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for proper padding (for immersive UI)
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

        // Set up spinners for course and year selection
        setupCourseSpinner()
        setupYearSpinner()

        // Navigate back to the login screen when the back button is clicked
        binding.backButton.setOnClickListener {
            finish()
        }

        // Handle user registration when the continue button is clicked
        binding.continueButton.setOnClickListener {
            handleSignUp()
        }
    }

    // Set up the course spinner with options from resources
    private fun setupCourseSpinner() {
        val courses = resources.getStringArray(R.array.course_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseSpinner.adapter = adapter

        // Update year options when a course is selected
        binding.courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateYearOptions(courses[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Set up the year spinner with default options
    private fun setupYearSpinner() {
        updateYearOptions("Select Course")
    }

    // Update the year spinner based on the selected course
    private fun updateYearOptions(selectedCourse: String) {
        val years = when (selectedCourse) {
            "SHS" -> arrayOf("Select Year", "G-11", "G-12")
            else -> arrayOf("Select Year", "1st", "2nd", "3rd", "4th")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.yearSpinner.adapter = adapter
    }

    // Handle sign-up process
    private fun handleSignUp() {
        // Retrieve input values from user
        val id = binding.idInput.text.toString().trim()
        val name = binding.nameInput.text.toString().trim()
        val course = binding.courseSpinner.selectedItem?.toString()?.trim() ?: ""
        val year = binding.yearSpinner.selectedItem?.toString()?.trim() ?: ""
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

        // Validate inputs; proceed if valid
        if (validateInputs(id, name, course, year, email, password, confirmPassword)) {
            showLoading(true) // Show loading indicator
            registerUser(email, password, id, name, course, year)
        }
    }

    // Validate user inputs for the sign-up form
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
                // Show error if any field is empty
                showToast("Please fill in all the fields")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                // Show error if email format is invalid
                showToast("Invalid email format")
                false
            }
            password.length < 6 -> {
                // Show error if password is too short
                showToast("Password must be at least 6 characters")
                false
            }
            password != confirmPassword -> {
                // Show error if passwords do not match
                showToast("Passwords do not match")
                false
            }
            else -> true
        }
    }

    // Register the user with Firebase Authentication
    private fun registerUser(email: String, password: String, id: String, name: String, course: String, year: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false) // Hide loading indicator
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Save user details to Firebase Realtime Database
                        saveUserToDatabase(userId, id, name, course, year, email)
                    } else {
                        showToast("User registration failed")
                    }
                } else {
                    // Handle registration errors
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    showToast(errorMessage)
                    Log.e("SignUpActivity", "Error: $errorMessage")
                }
            }
    }

    // Save user details to Firebase Realtime Database
    private fun saveUserToDatabase(userId: String, id: String, name: String, course: String, year: String, email: String) {
        val user = mapOf(
            "id" to id,
            "name" to name,
            "course" to course,
            "year" to year,
            "email" to email,
            "role" to "student" // Default role is "student"
        )

        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                // Show success message and navigate back to login
                showToast("Student registered successfully")
                finish()
            }
            .addOnFailureListener { e ->
                // Show error message if saving to database fails
                showToast("Failed to save user details")
                Log.e("SignUpActivity", "Error saving user to Realtime Database: ${e.message}")
            }
    }

    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Show or hide the loading indicator
    private fun showLoading(show: Boolean) {
        binding.signupProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
