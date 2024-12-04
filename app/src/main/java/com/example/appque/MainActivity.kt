package com.example.appque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.appque.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    // Declare global variables
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth and FirebaseDatabase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Auto-login if user is already authenticated and not logged out
        val currentUser = auth.currentUser
        val loggedOut = sharedPreferences.getBoolean("logged_out", false)
        if (currentUser != null && !loggedOut) {
            currentUser.reload()
                .addOnCompleteListener { reloadTask ->
                    if (reloadTask.isSuccessful) {
                        // Navigate based on user role
                        navigateBasedOnRole()
                    } else {
                        // Show toast and log the user out if session is expired
                        showCustomToast("Session expired. Please log in again.")
                        auth.signOut()
                    }
                }
        }

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val enteredEmail = binding.emailInput.text.toString().trim()
            val enteredPassword = binding.passwordInput.text.toString().trim()

            // Validate inputs; return if invalid
            if (!validateInputs(enteredEmail, enteredPassword)) return@setOnClickListener

            // Show loading indicator and log in the user
            showLoading(true)
            loginUser(enteredEmail, enteredPassword)
        }

        // Handle sign-up link click
        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Handle terms and privacy link click
        binding.termsPrivacyLink.setOnClickListener {
            val termsAndPrivacyContent = getString(R.string.terms_of_service_content) + "\n\n" + getString(R.string.privacy_policy_content)
            showPopup("Terms of Service and Privacy Policy", termsAndPrivacyContent)
        }
    }

    // Validate email and password inputs
    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || password.isEmpty() -> {
                // Show error if fields are empty
                showCustomToast("Please fill in all fields.")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                // Show error if email format is invalid
                showCustomToast("Invalid email format.")
                false
            }
            password.length < 6 -> {
                // Show error if password is too short
                showCustomToast("Password must be at least 6 characters.")
                false
            }
            else -> true
        }
    }

    // Log in the user using Firebase Authentication
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Update shared preferences and navigate based on role
                        sharedPreferences.edit().putBoolean("logged_out", false).apply()
                        navigateBasedOnRole()
                    } else {
                        showCustomToast("Login successful, but failed to retrieve user data.")
                    }
                } else {
                    // Handle login failure and show error messages
                    Log.e("MainActivity", "Login failed: ${task.exception?.message}")
                    val errorMessage = when (task.exception?.localizedMessage) {
                        "The password is invalid or the user does not have a password." -> "Invalid password. Please try again."
                        "There is no user record corresponding to this identifier." -> "No account found with this email."
                        else -> "Login failed. Please try again later."
                    }
                    showCustomToast(errorMessage)
                }
            }
    }

    // Navigate to the appropriate activity based on user role
    private fun navigateBasedOnRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Retrieve role from Firebase Realtime Database
            database.child("users").child(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").value?.toString() ?: "unknown"
                    navigateToActivityBasedOnRole(role)
                }
                .addOnFailureListener {
                    // Show error if role retrieval fails
                    showCustomToast("Error retrieving user role. Please try again.")
                }
        }
    }

    // Map user role to activity and navigate
    private fun navigateToActivityBasedOnRole(role: String) {
        val destination = when (role) {
            "admin" -> AdminActivity::class.java
            "staff1" -> CashierActivity::class.java
            "staff2" -> Window2Activity::class.java
            "staff3" -> Window3Activity::class.java
            "staff4" -> Window4Activity::class.java
            "staff5" -> Window5Activity::class.java
            "staff6" -> Window6Activity::class.java
            "staff7" -> Window7Activity::class.java
            "staff8" -> Window8Activity::class.java
            "student" -> StudentActivity::class.java
            else -> {
                // Default to MainActivity for unknown roles
                showCustomToast("Unknown role. Navigating back to login.")
                MainActivity::class.java
            }
        }
        // Start activity and finish current one
        startActivity(Intent(this, destination))
        finish()
    }

    // Show or hide loading indicator
    private fun showLoading(show: Boolean) {
        binding.loginProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    // Show a custom toast message
    private fun showCustomToast(message: String) {
        try {
            val inflater: LayoutInflater = layoutInflater
            val layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container))

            val text: TextView = layout.findViewById(R.id.toastText)
            text.text = message

            // Display the toast in the center of the screen
            Toast(applicationContext).apply {
                duration = Toast.LENGTH_SHORT
                view = layout
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        } catch (e: Exception) {
            // Fallback to default toast if custom toast fails
            Log.e("MainActivity", "Custom toast error: ${e.message}")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Show a popup with given title and message
    private fun showPopup(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Override back button to close the app
    override fun onBackPressed() {
        finishAffinity()
    }
}
