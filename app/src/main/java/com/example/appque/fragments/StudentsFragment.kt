
package com.example.appque.fragments
import Student
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appque.R
import com.example.appque.StudentsAdapter


class StudentsFragment : Fragment() {

        private lateinit var studentsAdapter: StudentsAdapter
        private val studentsList = mutableListOf<Student>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_students, container, false)

            // Setup RecyclerView and Adapter
            val recyclerView: RecyclerView = view.findViewById(R.id.studentsRecyclerView)
            studentsAdapter = StudentsAdapter(studentsList)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = studentsAdapter

            // Add button click listener
            val addStudentButton: ImageButton = view.findViewById(R.id.addStudentButton)
            addStudentButton.setOnClickListener {
                showAddStudentDialog()
            }

            return view
        }

        private fun showAddStudentDialog() {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_student, null)
            val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
            val alertDialog = dialogBuilder.create()

            val idInput = dialogView.findViewById<EditText>(R.id.idInput)
            val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
            val courseInput = dialogView.findViewById<EditText>(R.id.courseInput)
            val yearInput = dialogView.findViewById<EditText>(R.id.yearInput)
            val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
            val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
            val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val addButton = dialogView.findViewById<Button>(R.id.addButton)

            cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }

            addButton.setOnClickListener {
                val id = idInput.text.toString()
                val name = nameInput.text.toString()
                val course = courseInput.text.toString()
                val year = yearInput.text.toString()
                val email = usernameInput.text.toString()
                val password = passwordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                if (password == confirmPassword) {
                    val newStudent = Student(id, name, course, year, email)
                    studentsAdapter.addStudent(newStudent)
                    alertDialog.dismiss()
                } else {
                    confirmPasswordInput.error = "Passwords do not match"
                }
            }

            alertDialog.show()
        }
}

