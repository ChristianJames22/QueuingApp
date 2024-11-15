package com.example.appque

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appque.fragments.StudentsFragment
import com.example.appque.fragments.StaffFragment
import com.example.appque.fragments.RequestFragment

import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
