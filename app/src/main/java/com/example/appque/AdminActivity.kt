package com.example.appque

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.fragments.StudentsFragment
import com.example.appque.fragments.StaffFragment
import com.example.appque.fragments.RequestFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
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

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

}
