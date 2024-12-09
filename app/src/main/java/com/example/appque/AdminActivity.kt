package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appque.databinding.ActivityAdminBinding
import com.example.appque.fragments.ReminderStudentFragment
import com.example.appque.fragments.RequestFragment
import com.example.appque.fragments.StaffFragment
import com.example.appque.fragments.StudentsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentFragmentId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        verifyAdminRole()
        setupListeners()

        // Load the default fragment only if it's the first load
        if (savedInstanceState == null) {
            loadFragment(StudentsFragment(), R.id.nav_students)
        }
    }

    private fun verifyAdminRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.child("users").child(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").value?.toString()
                    if (role != "admin") {
                        showToast("Access Denied. Admin role required.")
                        redirectToLogin()
                    }
                }
                .addOnFailureListener {
                    showToast("Error verifying user role.")
                    redirectToLogin()
                }
        } else {
            redirectToLogin()
        }
    }


    private fun redirectToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupListeners() {
        // Logout button listener
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
                    else -> false
                }
            } else {
                false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, fragmentId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
        currentFragmentId = fragmentId
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                redirectToLogin()
                showToast("Logged out successfully.")
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.child("users").child(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").value?.toString()
                    if (role != "admin") {
                        showToast("Access Denied. Admin role required.")
                        redirectToLogin()
                    }
                }
                .addOnFailureListener {
                    showToast("Error verifying user role.")
                    redirectToLogin()
                }
        } else {
            redirectToLogin()
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
