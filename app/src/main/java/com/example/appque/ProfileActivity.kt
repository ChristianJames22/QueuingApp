package com.example.appque

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from Intent
        val userName = intent.getStringExtra("name") ?: "N/A"
        val userIdNumber = intent.getStringExtra("id") ?: "N/A"
        val userCourse = intent.getStringExtra("course") ?: "N/A"
        val userYear = intent.getStringExtra("year") ?: "N/A"

        Log.d(
            "ProfileActivity",
            "Profile Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )

        // Assign data to TextViews using View Binding
        binding.textName.text = "Name: $userName"
        binding.textIdNumber.text = "ID No.: $userIdNumber"
        binding.textCourse.text = "Course: $userCourse"
        binding.textYear.text = "Year: $userYear"
    }
}
