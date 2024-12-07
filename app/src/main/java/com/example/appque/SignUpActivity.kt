package com.example.appque

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appque.databinding.ActivitySignUpBinding
import com.example.appque.fragments.RequestFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SignUpActivity : AppCompatActivity() {

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

    private fun setupCourseSpinner() {
        val courses = resources.getStringArray(R.array.course_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseSpinner.adapter = adapter

        binding.courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateYearOptions(courses[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupYearSpinner() {
        updateYearOptions("Select Course")
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
            showLoading(true) // Show loading indicator
            checkIfIdNameOrEmailExists(id, name, email) { conflictMessage ->
                if (conflictMessage != null) {
                    showLoading(false)
                    Toast.makeText(this, conflictMessage, Toast.LENGTH_SHORT).show()
                } else {
                    submitRequestToFirebase(id, name, course, year, email, password)
                }
            }
        }
    }

    private fun checkIfIdNameOrEmailExists(id: String, name: String, email: String, callback: (String?) -> Unit) {
        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val existingId = child.child("id").value?.toString()
                        val existingName = child.child("name").value?.toString()
                        val existingEmail = child.child("email").value?.toString()

                        when {
                            existingId == id -> {
                                callback("ID already exists.")
                                return
                            }
                            existingName == name -> {
                                callback("Name already exists.")
                                return
                            }
                            existingEmail == email -> {
                                callback("Email already exists.")
                                return
                            }
                        }
                    }
                    callback(null) // No conflicts
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SignUpActivity, "Error checking ID, Name, or Email.", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            })
    }

    private fun submitRequestToFirebase(id: String, name: String, course: String, year: String, email: String, password: String) {
        val request = mapOf(
            "id" to id,
            "name" to name,
            "course" to course,
            "year" to year,
            "email" to email,
            "password" to password
        )

        // Add to temporary Firebase node
        database.child("temp_requests").push().setValue(request)
            .addOnSuccessListener {
                // Add to in-app `RequestFragment` list
                RequestFragment.requestList.add(request)
                showLoading(false)
                Toast.makeText(this, "Request submitted. Awaiting approval.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Failed to submit request. Try again.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showLoading(show: Boolean) {
        binding.signupProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.continueButton.isEnabled = !show
    }
}
