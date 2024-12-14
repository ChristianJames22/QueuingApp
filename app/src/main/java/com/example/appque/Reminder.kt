package com.example.appque.fragments

data class Reminder(
    val id: String = "",
    val title: String = "",
    val description: String = "", // Add this field
    val date: String = "", // Ensure you have a date field
    val time: String = "" // You can add more fields if needed
)
