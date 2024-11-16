package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class WindowSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_window_selection)

        // Initialize buttons and set up click listeners for each one
        findViewById<Button>(R.id.buttonCashier).setOnClickListener { navigateTo(StudentCashierActivity::class.java) }
        findViewById<Button>(R.id.buttonWindow1).setOnClickListener { navigateTo(StudentWindow1Activity::class.java) }
        findViewById<Button>(R.id.buttonWindow2).setOnClickListener { navigateTo(StudentWindow2Activity::class.java) }
        findViewById<Button>(R.id.buttonWindow3).setOnClickListener { navigateTo(StudentWindow3Activity::class.java) }
        findViewById<Button>(R.id.buttonWindow4).setOnClickListener { navigateTo(StudentWindow4Activity::class.java) }
        findViewById<Button>(R.id.buttonWindow5).setOnClickListener { navigateTo(StudentWindow5Activity::class.java) }
        findViewById<Button>(R.id.buttonWindow6).setOnClickListener { navigateTo(StudentWindow6Activity::class.java) }
        findViewById<Button>(R.id.buttonWindow7).setOnClickListener { navigateTo(StudentWindow7Activity::class.java) }
        findViewById<Button>(R.id.buttonWindow8).setOnClickListener { navigateTo(StudentWindow8Activity::class.java) }

    }

    // Function to handle navigation to different activities
    private fun <T> navigateTo(destination: Class<T>) {
        val intent = Intent(this, destination)
        startActivity(intent)
    }

    // Override the back button behavior to exit the app
    override fun onBackPressed() {
        super.onBackPressed()
        // Close all activities and exit the application
        finishAffinity()
    }
}
