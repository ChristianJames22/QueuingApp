package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WindowSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_window_selection)  // Your window selection layout

        // Buttons for selecting different windows
        val buttonCashier = findViewById<Button>(R.id.buttonCashier)
        val buttonWindow1 = findViewById<Button>(R.id.buttonWindow1)
        val buttonWindow2 = findViewById<Button>(R.id.buttonWindow2)
        val buttonWindow3 = findViewById<Button>(R.id.buttonWindow3)
        val buttonWindow4 = findViewById<Button>(R.id.buttonWindow4)

        // Set up the Cashier button to navigate to the Cashier Activity
        buttonCashier.setOnClickListener {
            val intent = Intent(this, StudentCashierActivity::class.java)  // Navigate to StudentCashierActivity
            startActivity(intent)
        }

        // Set up the Window 1 button to navigate to another activity if needed
        buttonWindow1.setOnClickListener {
            // Example: if needed, you can navigate to a different activity for Window 1.
            // val intent = Intent(this, AnotherActivity::class.java)
            // startActivity(intent)
        }

        // Set up listeners for other windows (buttonWindow2, buttonWindow3, buttonWindow4)
        buttonWindow2.setOnClickListener {
            // Handle navigation for Window 2
        }

        buttonWindow3.setOnClickListener {
            // Handle navigation for Window 3
        }

        buttonWindow4.setOnClickListener {
            // Handle navigation for Window 4
        }
    }
}
