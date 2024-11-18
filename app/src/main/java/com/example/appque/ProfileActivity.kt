package com.example.appque

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userName = intent.getStringExtra("name") ?: "N/A"
        val userIdNumber = intent.getStringExtra("idNumber") ?: "N/A"
        val userCourse = intent.getStringExtra("course") ?: "N/A"
        val userYear = intent.getStringExtra("year") ?: "N/A"

        Log.d(
            "ProfileActivity",
            "Profile Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )

        findViewById<TextView>(R.id.textName).text = "Name: $userName"
        findViewById<TextView>(R.id.textIdNumber).text = "ID No.: $userIdNumber"
        findViewById<TextView>(R.id.textCourse).text = "Course: $userCourse"
        findViewById<TextView>(R.id.textYear).text = "Year: $userYear"
    }
}
