package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appque.databinding.ActivityAdminBinding
import com.example.appque.fragments.InactiveFragment
import com.example.appque.fragments.ReminderStudentFragment
import com.example.appque.fragments.RequestFragment
import com.example.appque.fragments.StaffFragment
import com.example.appque.fragments.StudentsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private var binding: ActivityAdminBinding? = null
    private var database: DatabaseReference? = null
    private var auth: FirebaseAuth? = null
    private var currentFragmentId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        try {
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance().reference

            setupListeners()

            // Load the default fragment only if it's the first load
            if (savedInstanceState == null) {
                loadFragment(StudentsFragment(), R.id.nav_students)
            }
        } catch (e: Exception) {
            showToast("Error initializing activity: ${e.localizedMessage}")
        }
    }

    private fun redirectToLogin() {
        try {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } catch (e: Exception) {
            showToast("Error redirecting to login: ${e.localizedMessage}")
        }
    }

    private fun setupListeners() {
        try {
            // Logout button listener
            binding?.logoutButton?.setOnClickListener {
                showLogoutConfirmationDialog()
            }

            // Bottom navigation listener
            binding?.bottomNavigation?.setOnItemSelectedListener { item ->
                if (currentFragmentId != item.itemId) {
                    when (item.itemId) {
                        R.id.nav_students -> {
                            loadFragment(StudentsFragment(), item.itemId)
                            true
                        }
                        R.id.nav_staff -> {
                            loadFragment(StaffFragment(), item.itemId)
                            true
                        }
                        R.id.nav_request -> {
                            loadFragment(RequestFragment(), item.itemId)
                            true
                        }
                        R.id.nav_reminders -> {
                            loadFragment(ReminderStudentFragment<Any>(), item.itemId)
                            true
                        }
                        R.id.nav_inactive -> {
                            loadFragment(InactiveFragment(), item.itemId)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            showToast("Error setting up listeners: ${e.localizedMessage}")
        }
    }

    private fun loadFragment(fragment: Fragment, fragmentId: Int) {
        try {
            binding?.fragmentContainer?.let {
                supportFragmentManager.beginTransaction()
                    .replace(it.id, fragment)
                    .commit()
            }
            currentFragmentId = fragmentId
        } catch (e: Exception) {
            showToast("Error loading fragment: ${e.localizedMessage}")
        }
    }

    private fun showLogoutConfirmationDialog() {
        try {
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        auth?.signOut()
                        redirectToLogin()
                        showToast("Logged out successfully.")
                    } catch (e: Exception) {
                        showToast("")
                    }
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        } catch (e: Exception) {
            showToast("Error showing logout confirmation: ${e.localizedMessage}")
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            val currentUser = auth?.currentUser
            if (currentUser == null) {
                redirectToLogin()
            }
        } catch (e: Exception) {
            showToast("Error during start: ${e.localizedMessage}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
