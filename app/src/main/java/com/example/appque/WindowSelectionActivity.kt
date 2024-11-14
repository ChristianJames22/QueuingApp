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
        val buttonWindow5 = findViewById<Button>(R.id.buttonWindow5)
        val buttonWindow6 = findViewById<Button>(R.id.buttonWindow6)
        val buttonWindow7 = findViewById<Button>(R.id.buttonWindow7)
        val buttonWindow8 = findViewById<Button>(R.id.buttonWindow8)

        // Set up your button click listeners
        buttonCashier.setOnClickListener {
            val intent = Intent(this, StudentCashierActivity::class.java)
            startActivity(intent)
        }

        buttonWindow1.setOnClickListener {
            val intent = Intent(this, StudentWindow1Activity::class.java)
            startActivity(intent)
        }

        buttonWindow2.setOnClickListener {
            val intent = Intent(this, StudentWindow2Activity::class.java)
            startActivity(intent)
        }

        buttonWindow3.setOnClickListener {
            val intent = Intent(this, StudentWindow3Activity::class.java)
            startActivity(intent)
        }

        buttonWindow4.setOnClickListener {
            val intent = Intent(this, StudentWindow4Activity::class.java)
            startActivity(intent)
        }

        buttonWindow5.setOnClickListener {
            val intent = Intent(this, StudentWindow5Activity::class.java)
            startActivity(intent)
        }

        buttonWindow6.setOnClickListener {
            val intent = Intent(this, StudentWindow6Activity::class.java)
            startActivity(intent)
        }

        buttonWindow7.setOnClickListener {
            val intent = Intent(this, StudentWindow7Activity::class.java)
            startActivity(intent)
        }

        buttonWindow8.setOnClickListener {
            val intent = Intent(this, StudentWindow8Activity::class.java)
            startActivity(intent)
        }
    }

    // Override the back button behavior to exit the app
    override fun onBackPressed() {
        // Exit the application when the back button is pressed on WindowSelectionActivity
        finishAffinity()  // This will close the app
    }
}
