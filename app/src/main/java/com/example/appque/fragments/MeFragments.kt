package com.example.appque.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appque.databinding.FragmentsMeBinding

class MeFragments : Fragment() {

    private var _binding: FragmentsMeBinding? = null
    private val binding get() = _binding!!

    private var userName: String? = null
    private var userIdNumber: String? = null
    private var userCourse: String? = null
    private var userYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve user data from arguments
        arguments?.let {
            userName = it.getString("name")
            userIdNumber = it.getString("id")
            userCourse = it.getString("course")
            userYear = it.getString("year")
        }

        Log.d(
            "MeFragments",
            "Received Data -> Name: $userName, ID: $userIdNumber, Course: $userCourse, Year: $userYear"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentsMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = arguments?.getString("name") ?: "N/A"
        val userIdNumber = arguments?.getString("id") ?: "N/A"
        val userCourse = arguments?.getString("course") ?: "N/A"
        val userYear = arguments?.getString("year") ?: "N/A"

        binding.textName.text = "Name: $userName"
        binding.textIdNumber.text = "ID No.: $userIdNumber"
        binding.textCourse.text = "Course: $userCourse"
        binding.textYear.text = "Year: $userYear"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
