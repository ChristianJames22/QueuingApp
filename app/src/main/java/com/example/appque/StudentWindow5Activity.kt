package com.example.appque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow5Binding

class StudentWindow5Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow5Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentWindow5Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back arrow button functionality
        binding.backArrowButton.setOnClickListener {
            finish()
        }
    }
}
