package com.example.appque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow8Binding

class StudentWindow8Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow8Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentWindow8Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back arrow button functionality
        binding.backArrowButton.setOnClickListener {
            finish()
        }
    }
}
