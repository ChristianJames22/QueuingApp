package com.example.appque

import Student
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class StudentsAdapter(
    private val studentsList: MutableList<Student>,
    private val onItemClicked: (Student) -> Unit
) : RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentsList[position]
        holder.nameTextView.text = student.name
        holder.idTextView.text = "ID: ${student.id}"
        holder.emailTextView.text = "Email: ${student.email}"
        holder.courseTextView.text = "Course: ${student.course}"
        holder.yearTextView.text = "Year: ${student.year}"

        // Format and display the timestamp
        val date = Date(student.timestamp)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = formatter.format(date)
        holder.timestampTextView.text = "Added on: $formattedDate"

        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClicked(student)
        }
    }

    override fun getItemCount() = studentsList.size

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val courseTextView: TextView = itemView.findViewById(R.id.courseTextView)
        val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
    }
}
