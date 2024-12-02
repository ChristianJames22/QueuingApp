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
import androidx.appcompat.app.AppCompatActivity
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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Auto-login if the user is already authenticated
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

        binding.loginButton.setOnClickListener {
            val enteredEmail = binding.emailInput.text.toString().trim()
            val enteredPassword = binding.passwordInput.text.toString().trim()

            if (!validateInputs(enteredEmail, enteredPassword)) return@setOnClickListener

            showLoading(true)
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
                showLoading(false)
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        sharedPreferences.edit().putBoolean("logged_out", false).apply()
                        navigateBasedOnRole()
                    } else {
                        showCustomToast("Login successful, but failed to retrieve user data.")
                    }
                } else {
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
                showCustomToast("Unknown role. Navigating back to login.")
                MainActivity::class.java
            }
        }
        startActivity(Intent(this, destination))
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.loginProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

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

    override fun onBackPressed() {
        finishAffinity()
    }
}
