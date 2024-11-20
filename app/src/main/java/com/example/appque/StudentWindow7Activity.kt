package com.example.appque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow7Binding

class StudentWindow7Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow7Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentWindow7Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back arrow button functionality
        binding.backArrowButton.setOnClickListener {
            finish()
        }
    }
}
