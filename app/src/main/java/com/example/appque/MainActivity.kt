package com.example.appque

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton: Button = findViewById(R.id.loginButton)
        val idInput: EditText = findViewById(R.id.idInput)

        loginButton.setOnClickListener {
            val enteredId = idInput.text.toString().trim()
            if (enteredId.isNotEmpty()) {
                val intent = when (enteredId) {
                    "0001" -> Intent(this, CashierActivity::class.java)
                    "0002" -> Intent(this, Window1Activity::class.java)
                    "0003" -> Intent(this, Window2Activity::class.java)
                    "0004" -> Intent(this, Window3Activity::class.java)
                    "0005" -> Intent(this, Window4Activity::class.java)
                    else -> {
                        Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                intent.putExtra("toggledId", enteredId)
                startActivity(intent)
                finish()  // Close MainActivity
            } else {
                Toast.makeText(this, "Please enter a valid ID", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
