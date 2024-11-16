package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    data class User(
        val name: String,
        val password: String,
        val idNumber: String? = null,
        val course: String? = null,
        val year: String? = null
    )

    // Map of emails to User objects
    private val emailToUserMap = mapOf(
        "Alice@gmail.com" to User(
            name = "Alice",
            password = "2024-01",
            idNumber = "22-01",
            course = "BSIT",
            year = "3rd Year"
        ),
        "Staff1@gmail.com" to User(name = "Staff1", password = "0001"),
        "Staff2@gmail.com" to User(name = "Staff2", password = "0002"),
        "Staff3@gmail.com" to User(name = "Staff3", password = "0003"),
        "Staff4@gmail.com" to User(name = "Staff4", password = "0004"),
        "Staff5@gmail.com" to User(name = "Staff5", password = "0005"),
        "Staff6@gmail.com" to User(name = "Staff6", password = "0006"),
        "Staff7@gmail.com" to User(name = "Staff7", password = "0007"),
        "Staff8@gmail.com" to User(name = "Staff8", password = "0008"),
        "Mainadmin@gmail.com" to User(name = "Main admin", password = "2023")
    )

    // Map of emails to Activities
    private val emailToActivityMap = mapOf(
        "Staff1@gmail.com" to CashierActivity::class.java,
        "Staff2@gmail.com" to Window1Activity::class.java,
        "Staff3@gmail.com" to Window2Activity::class.java,
        "Staff4@gmail.com" to Window3Activity::class.java,
        "Staff5@gmail.com" to Window4Activity::class.java,
        "Staff6@gmail.com" to Window5Activity::class.java,
        "Staff7@gmail.com" to Window6Activity::class.java,
        "Staff8@gmail.com" to Window7Activity::class.java
    )

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val enteredEmail = binding.emailInput.text.toString().trim()
            val enteredPassword = binding.passwordInput.text.toString().trim()

            val user = emailToUserMap[enteredEmail]

            if (user != null && enteredPassword == user.password) {
                Log.d("MainActivity", "Login successful for email: $enteredEmail")

                // Determine which activity to navigate to based on email
                val destinationActivity = emailToActivityMap[enteredEmail] ?: AdminActivity::class.java

                val intent = Intent(this, destinationActivity).apply {
                    putExtra("name", user.name)
                    putExtra("idNumber", user.idNumber)
                    putExtra("course", user.course)
                    putExtra("year", user.year)
                }

                startActivity(intent)
                finish()
            } else {
                // Show a custom toast for invalid login
                showCustomToast("Invalid email or password. Please try again.")
            }
        }

        binding.signUpLink.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
    }

    // Function to display a custom toast
    private fun showCustomToast(message: String) {
        val inflater: LayoutInflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container))

        val text: TextView = layout.findViewById(R.id.toastText)
        text.text = message

        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.CENTER, 0, 0) // Center the toast
        toast.show()
    }
}
