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
import com.google.firebase.database.*
import android.text.format.DateFormat

class SignUpActivity : AppCompatActivity() {

    private  var binding: ActivitySignUpBinding? = null
    private  var database: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference

        // Inflate the activity layout
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Handle window insets for immersive UI
        ViewCompat.setOnApplyWindowInsetsListener(binding!!.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.main.setPadding(
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

        // Navigate back to the login screen
        binding!!.backButton.setOnClickListener {
            finish()
        }

        // Handle user registration
        binding!!.continueButton.setOnClickListener {
            handleSignUp()
        }
    }

    private fun setupCourseSpinner() {
        val courses = resources.getStringArray(R.array.course_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.courseSpinner?.adapter = adapter

        binding?.courseSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
        binding?.yearSpinner?.adapter = adapter
    }

    private fun handleSignUp() {
        try {
            val id = binding?.idInput?.text.toString().trim()
            val name = binding?.nameInput?.text.toString().trim()
            val course = binding?.courseSpinner?.selectedItem?.toString()?.trim() ?: ""
            val year = binding?.yearSpinner?.selectedItem?.toString()?.trim() ?: ""
            val email = binding?.emailInput?.text.toString().trim()
            val password = binding?.passwordInput?.text.toString().trim()
            val confirmPassword = binding?.confirmPasswordInput?.text.toString().trim()

            if (validateInputs(id, name, course, year, email, password, confirmPassword)) {
                showLoading(true)
                checkForDuplicateEntries(id, name, email, course, year, password)
            }
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForDuplicateEntries(
        id: String,
        name: String,
        email: String,
        course: String,
        year: String,
        password: String
    ) {
        database?.child("users")?.orderByChild("name")?.equalTo(name)?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userNameSnapshot: DataSnapshot) {
                if (userNameSnapshot.exists()) {
                    showLoading(false)
                    Toast.makeText(this@SignUpActivity, "Name already exists.", Toast.LENGTH_SHORT).show()
                    return
                }
                database!!.child("users").orderByChild("id").equalTo(id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userIdSnapshot: DataSnapshot) {
                        if (userIdSnapshot.exists()) {
                            showLoading(false)
                            Toast.makeText(this@SignUpActivity, "ID already exists.", Toast.LENGTH_SHORT).show()
                            return
                        }
                        database!!.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userEmailSnapshot: DataSnapshot) {
                                if (userEmailSnapshot.exists()) {
                                    showLoading(false)
                                    Toast.makeText(this@SignUpActivity, "Email already exists.", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                database!!.child("pending_requests").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(pendingSnapshot: DataSnapshot) {
                                        if (pendingSnapshot.exists()) {
                                            showLoading(false)
                                            Toast.makeText(this@SignUpActivity, "Request already submitted.", Toast.LENGTH_SHORT).show()
                                            return
                                        }
                                        submitRequestToFirebase(id, name, course, year, email, password)
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        showLoading(false)
                                        Toast.makeText(this@SignUpActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                showLoading(false)
                                Toast.makeText(this@SignUpActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showLoading(false)
                        Toast.makeText(this@SignUpActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(this@SignUpActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitRequestToFirebase(
        id: String,
        name: String,
        course: String,
        year: String,
        email: String,
        password: String
    ) {
        val timestamp = System.currentTimeMillis()

        // Exclude `formattedTimestamp`
        val request = mapOf(
            "id" to id,
            "name" to name,
            "course" to course,
            "year" to year,
            "email" to email,
            "password" to password,
            "timestamp" to timestamp
        )

        database?.child("pending_requests")?.push()?.setValue(request)
            ?.addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Request submitted. Awaiting admin approval.", Toast.LENGTH_SHORT).show()
                finish()
            }
            ?.addOnFailureListener { exception ->
                showLoading(false)
                Toast.makeText(this, "Error submitting request: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showLoading(show: Boolean) {
        binding?.signupProgressBar?.visibility = if (show) View.VISIBLE else View.GONE
        binding?.continueButton?.isEnabled = !show
    }
}
