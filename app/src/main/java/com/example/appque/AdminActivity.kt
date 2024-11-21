package com.example.appque

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appque.databinding.ActivityAdminBinding
import com.example.appque.fragments.RequestFragment
import com.example.appque.fragments.StaffFragment
import com.example.appque.fragments.StudentsFragment
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Logout Button
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Set up BottomNavigationView with View Binding
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_students -> {
                    loadFragment(StudentsFragment())
                    true
                }
                R.id.nav_staff -> {
                    loadFragment(StaffFragment())
                    true
                }
                R.id.nav_request -> {
                    loadFragment(RequestFragment())
                    true
                }
                else -> false
            }
        }

        // Load the default fragment
        loadFragment(StudentsFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
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