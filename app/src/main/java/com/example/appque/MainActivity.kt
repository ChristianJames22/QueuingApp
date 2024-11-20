package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.SharedPreferences
import android.view.View
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private var preventAutoLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        preventAutoLogin = sharedPreferences.getBoolean("logged_out", false)

        binding.loginButton.setOnClickListener {
            val enteredEmail = binding.emailInput.text.toString().trim()
            val enteredPassword = binding.passwordInput.text.toString().trim()

            if (!validateInputs(enteredEmail, enteredPassword)) return@setOnClickListener

            showLoading(true) // Show the loading circle
            loginUser(enteredEmail, enteredPassword)
        }

        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || password.isEmpty() -> {
                showCustomToast("Please fill in all fields.")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showCustomToast("Invalid email format.")
                false
            }
            password.length < 6 -> {
                showCustomToast("Password must be at least 6 characters.")
                false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false) // Hide the loading circle
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        fetchUserRole(it)
                    } ?: showCustomToast("Failed to retrieve user ID.")
                } else {
                    Log.e("MainActivity", "Login failed: ${task.exception?.message}")
                    showCustomToast("Invalid email or password. Please try again.")
                }
            }
    }

    private fun fetchUserRole(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "student"
                    val name = document.getString("name") ?: "User"
                    val id = document.getString("id") ?: "Unknown"
                    val course = document.getString("course")
                    val year = document.getString("year")

                    Log.d("MainActivity", "User data -> Name: $name, ID: $id, Course: $course, Year: $year")
                    navigateBasedOnRole(role, name, id, course, year)
                } else {
                    showCustomToast("No user details found. Please contact admin.")
                }
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Error fetching user role: ${it.message}")
                showCustomToast("Failed to fetch user details.")
            }
    }

    private fun navigateBasedOnRole(role: String, name: String, id: String, course: String?, year: String?) {
        val destination = when (role) {
            "admin" -> AdminActivity::class.java
            "staff" -> CashierActivity::class.java
            "staff2" -> Window2Activity::class.java
            "staff3" -> Window3Activity::class.java
            "staff4" -> Window4Activity::class.java
            "staff5" -> Window5Activity::class.java
            "staff6" -> Window6Activity::class.java
            "staff7" -> Window7Activity::class.java
            "staff8" -> Window8Activity::class.java
            "student" -> StudentActivity::class.java
            else -> MainActivity::class.java
        }

        val intent = Intent(this, destination).apply {
            putExtra("name", name)
            putExtra("id", id)
            putExtra("course", course)
            putExtra("year", year)
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.loginProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showCustomToast(message: String) {
        val inflater: LayoutInflater = layoutInflater
        val layout =
            inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container))

        val text: TextView = layout.findViewById(R.id.toastText)
        text.text = message

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}