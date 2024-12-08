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

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Auto-login if user session exists
        val currentUser = auth.currentUser
        val loggedOut = sharedPreferences.getBoolean("logged_out", false)
        if (currentUser != null && !loggedOut) {
            currentUser.reload()
                .addOnCompleteListener { reloadTask ->
                    if (reloadTask.isSuccessful) {
                        navigateBasedOnRole()
                    } else {
                        showCustomToast("Session expired. Please log in again.")
                        auth.signOut()
                    }
                }
        }

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val enteredEmail = binding.emailInput.text.toString().trim()
            val enteredPassword = binding.passwordInput.text.toString().trim()

            if (!validateInputs(enteredEmail, enteredPassword)) return@setOnClickListener

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

    // Validate email and password input
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

    // Authenticate user and validate against Firebase Authentication
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        validateUserInDatabase(userId)
                    } else {
                        showCustomToast("Login successful, but failed to retrieve user data.")
                    }
                } else {
                    Log.e("MainActivity", "Authentication failed: ${task.exception?.message}")
                    showCustomToast("Invalid email or password.")
                }
            }
    }

    // Validate user in Firebase Realtime Database
    private fun validateUserInDatabase(userId: String) {
        database.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    sharedPreferences.edit().putBoolean("logged_out", false).apply()
                    val role = snapshot.child("role").value?.toString() ?: "unknown"
                    navigateToActivityBasedOnRole(role)
                } else {
                    showCustomToast("Invalid email or password.")
                    auth.signOut()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Database error: ${exception.message}")
                showCustomToast("Login failed. Please try again later.")
            }
    }

    // Navigate based on user role
    private fun navigateBasedOnRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.child("users").child(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").value?.toString() ?: "unknown"
                    navigateToActivityBasedOnRole(role)
                }
                .addOnFailureListener {
                    showCustomToast("Error retrieving user role. Please try again.")
                }
        }
    }

    // Navigate user to appropriate activity based on their role
    private fun navigateToActivityBasedOnRole(role: String) {
        val destination = when (role) {
            "admin" -> AdminActivity::class.java
            "window1" -> CashierActivity::class.java
            "window2" -> Window2Activity::class.java
            "window3" -> Window3Activity::class.java
            "window4" -> Window4Activity::class.java
            "window5" -> Window5Activity::class.java
            "window6" -> Window6Activity::class.java
            "window7" -> Window7Activity::class.java
            "window8" -> Window8Activity::class.java
            "student" -> StudentActivity::class.java
            else -> {
                showCustomToast("Unknown role. Navigating back to login.")
                MainActivity::class.java
            }
        }
        startActivity(Intent(this, destination))
        finish()
    }

    // Show or hide loading indicator
    private fun showLoading(show: Boolean) {
        binding.loginProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    // Custom toast message
    private fun showCustomToast(message: String) {
        try {
            val inflater: LayoutInflater = layoutInflater
            val layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container))

            val text: TextView = layout.findViewById(R.id.toastText)
            text.text = message

            Toast(applicationContext).apply {
                duration = Toast.LENGTH_SHORT
                view = layout
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Custom toast error: ${e.message}")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Show popup for terms and privacy
    private fun showPopup(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
