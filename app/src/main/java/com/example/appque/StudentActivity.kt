package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appque.databinding.ActivityStudentBinding
import com.example.appque.fragments.MeFragments
import com.example.appque.fragments.RemindersFragment
import com.example.appque.fragments.WindowsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Retrieve the current user ID
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Fetch user data from Firebase Realtime Database
            database.child("users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val userName = snapshot.child("name").value?.toString() ?: "N/A"
                    val userCourse = snapshot.child("course").value?.toString() ?: "N/A"
                    val userYear = snapshot.child("year").value?.toString() ?: "N/A"

                    // Pass user data to fragments via BottomNavigationView
                    setupBottomNavigation(userName, userId, userCourse, userYear)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up Logout Button
        val logoutButton: ImageButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Load default fragment
        loadFragment(WindowsFragment())
    }

    private fun setupBottomNavigation(name: String, id: String, course: String, year: String) {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_windows -> {
                    loadFragment(WindowsFragment()) // Windows fragment
                    true
                }
                R.id.nav_reminders -> {
                    loadFragment(RemindersFragment()) // Reminders fragment
                    true
                }
                R.id.nav_me -> {
                    val meFragment = MeFragments().apply {
                        arguments = Bundle().apply {
                            putString("name", name)
                            putString("id", id)
                            putString("course", course)
                            putString("year", year)
                        }
                    }
                    loadFragment(meFragment) // Me fragment with user data
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()

                // Navigate to MainActivity (login screen)
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finishAffinity()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
