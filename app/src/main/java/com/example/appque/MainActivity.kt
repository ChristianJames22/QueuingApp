package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Map of IDs to names
    private val idToNameMap = mapOf(
        // Students
        "2024-01" to "Alice",

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

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use binding to access views
        binding.loginButton.setOnClickListener {
            val enteredId = binding.idInput.text.toString().trim()
            val name = idToNameMap[enteredId]

            if (enteredId.isNotEmpty() && name != null) {
                // Start the appropriate activity based on ID
                val intent = when (enteredId) {
                    "2024-01" -> Intent(this, WindowSelectionActivity::class.java)
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
                intent.putExtra("name", name)
                startActivity(intent)
                finish() // Close MainActivity
            } else {
                Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
