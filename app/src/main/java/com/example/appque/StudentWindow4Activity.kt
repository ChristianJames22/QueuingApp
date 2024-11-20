package com.example.appque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appque.databinding.ActivityStudentWindow4Binding

class StudentWindow4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentWindow4Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentWindow4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back arrow button functionality
        binding.backArrowButton.setOnClickListener {
            finish()
        }
    }
}
