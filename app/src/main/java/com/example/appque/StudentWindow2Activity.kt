package com.example.appque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow2Binding

class StudentWindow2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentWindow2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back arrow button functionality
        binding.backArrowButton.setOnClickListener {
            finish()
        }
    }
}
