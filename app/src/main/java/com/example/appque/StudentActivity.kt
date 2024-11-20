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

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve user data passed from the previous activity
        val userName = intent.getStringExtra("name") ?: "N/A"
        val userId = intent.getStringExtra("id") ?: "N/A"
        val userCourse = intent.getStringExtra("course") ?: "N/A"
        val userYear = intent.getStringExtra("year") ?: "N/A"

        // Set up BottomNavigationView
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
                            putString("name", userName)
                            putString("id", userId)
                            putString("course", userCourse)
                            putString("year", userYear)
                        }
                    }
                    loadFragment(meFragment) // Me fragment with user data
                    true
                }
                else -> false
            }
        }

        // Set up Logout Button
        val logoutButton: ImageButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Load default fragment
        loadFragment(WindowsFragment())
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
