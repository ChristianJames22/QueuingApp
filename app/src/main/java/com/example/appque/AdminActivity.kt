package com.example.appque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appque.databinding.ActivityAdminBinding
import com.example.appque.fragments.RequestFragment
import com.example.appque.fragments.StaffFragment
import com.example.appque.fragments.StudentsFragment

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}
