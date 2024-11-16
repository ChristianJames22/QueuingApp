package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    // Map of emails (name@gmail.com) to IDs
    private val emailToIdMap = mapOf(
        "Alice@gmail.com" to "2024-01",

        // Admins
        "Admin1@gmail.com" to "0001",
        "Admin2@gmail.com" to "0002",
        "Admin3@gmail.com" to "0003",
        "Admin4@gmail.com" to "0004",
        "Admin5@gmail.com" to "0005",
        "Admin6@gmail.com" to "0006",
        "Admin7@gmail.com" to "0007",
        "Admin8@gmail.com" to "0008",
        "Mainadmin@gmail.com" to "2023"  // This is the main admin account
    )

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val enteredEmail = binding.emailInput.text.toString().trim()
            val enteredPassword = binding.passwordInput.text.toString().trim()

            // Retrieve the ID (password) based on the entered email
            val expectedPassword = emailToIdMap[enteredEmail]

            if (expectedPassword != null && enteredPassword == expectedPassword) {
                // Start the appropriate activity based on the entered password
                val intent = when (enteredPassword) {
                    "2024-01" -> Intent(this, WindowSelectionActivity::class.java)  // Redirect to StudentWindow1Activity
                    "0001" -> Intent(this, CashierActivity::class.java)
                    "0002" -> Intent(this, Window1Activity::class.java)
                    "0003" -> Intent(this, Window2Activity::class.java)
                    "0004" -> Intent(this, Window3Activity::class.java)
                    "0005" -> Intent(this, Window4Activity::class.java)
                    "0006" -> Intent(this, Window5Activity::class.java)
                    "0007" -> Intent(this, Window6Activity::class.java)
                    "0008" -> Intent(this, Window7Activity::class.java)
                    "2023" -> Intent(this, AdminActivity::class.java) // Admin activity for main admin
                    else -> {
                        Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                // Pass the ID and name to the next activity
                intent.putExtra("toggledId", enteredPassword)
                intent.putExtra("name", enteredEmail.split("@")[0])  // Extract the name from the email
                startActivity(intent)
                finish() // Close MainActivity after starting the next activity
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle "Sign up" text click to navigate to SignUpActivity
        binding.signUpLink.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
    }
}