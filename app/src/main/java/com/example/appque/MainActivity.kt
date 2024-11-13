package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // Map of IDs to names
    private val idToNameMap = mapOf(
        // Students
        "2024-01" to "Alice",
        "2024-02" to "Bob",
        "2024-03" to "Charlie",
        "2024-04" to "Diana",
        "2024-05" to "Eve",
        "2024-06" to "Frank",
        "2024-07" to "Grace",
        // Admins
        "0001" to "Admin1",
        "0002" to "Admin2",
        "0003" to "Admin3",
        "0004" to "Admin4",
        "0005" to "Admin5",
        "0006" to "Admin6",
        "0007" to "Admin7",
        "0008" to "Admin8"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton: Button = findViewById(R.id.loginButton)
        val idInput: EditText = findViewById(R.id.idInput)

        loginButton.setOnClickListener {
            val enteredId = idInput.text.toString().trim()
            val name = idToNameMap[enteredId]

            if (enteredId.isNotEmpty() && name != null) {
                // Start the appropriate activity based on ID
                val intent = when (enteredId) {
                    "2024-01", "2024-02", "2024-03", "2024-04", "2024-05", "2024-06", "2024-07" -> Intent(this, WindowSelectionActivity::class.java)
                    "0001" -> Intent(this, CashierActivity::class.java)
                    "0002" -> Intent(this, Window1Activity::class.java)
                    "0003" -> Intent(this, Window2Activity::class.java)
                    "0004" -> Intent(this, Window3Activity::class.java)
                    "0005" -> Intent(this, Window4Activity::class.java)
                    "0006" -> Intent(this, Window5Activity::class.java)
                    "0007" -> Intent(this, Window6Activity::class.java)
                    "0008" -> Intent(this, Window7Activity::class.java)
                    else -> {
                        Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                // Pass the ID and name to the next activity
                intent.putExtra("toggledId", enteredId)
                intent.putExtra("name", name)  // Pass the name as well
                startActivity(intent)
                finish() // Close MainActivity
            } else {
                Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
