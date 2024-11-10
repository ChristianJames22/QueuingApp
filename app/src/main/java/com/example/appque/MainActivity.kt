package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton: Button = findViewById(R.id.loginButton)
        val idInput: EditText = findViewById(R.id.idInput)

        loginButton.setOnClickListener {
            val enteredId = idInput.text.toString().trim()
            if (enteredId.isNotEmpty()) {
                // Checking if the entered ID is 2024-01 for WindowSelectionActivity
                val intent = when (enteredId) {
                    "2024-01" -> Intent(this, WindowSelectionActivity::class.java)  // New condition for 2024-01
                    "0001" -> Intent(this, CashierActivity::class.java)
                    "0002" -> Intent(this, Window1Activity::class.java)
                    "0003" -> Intent(this, Window2Activity::class.java)
                    "0004" -> Intent(this, Window3Activity::class.java)
                    "0005" -> Intent(this, Window4Activity::class.java)
                    else -> {
                        Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                intent.putExtra("toggledId", enteredId)  // Optionally pass the ID for use in the next activity
                startActivity(intent)
                finish()  // Close MainActivity
            } else {
                Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
