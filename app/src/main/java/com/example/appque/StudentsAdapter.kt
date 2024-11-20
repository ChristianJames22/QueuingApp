package com.example.appque

import Student
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentsAdapter(private val studentsList: MutableList<Student>) :
    RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentsList[position]
        holder.nameTextView.text = student.name // Dynamically set student name
        holder.idTextView.text = "ID: ${student.id}" // Add "ID:" dynamically
        holder.emailTextView.text = "Email: ${student.email}" // Add "Email:" dynamically
        holder.courseTextView.text = "Course: ${student.course}" // Add "Course:" dynamically
        holder.yearTextView.text = "Year: ${student.year}" // Add "Year:" dynamically
    }

    override fun getItemCount() = studentsList.size

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val courseTextView: TextView = itemView.findViewById(R.id.courseTextView)
        val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
    }
}