package com.example.appque.fragments

import Staff // Ensure this is the correct model class
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appque.R
import com.example.appque.StaffAdapter
import com.example.appque.databinding.FragmentStaffBinding

class StaffFragment : Fragment() {

    private var _binding: FragmentStaffBinding? = null
    private val binding get() = _binding!!

    private lateinit var staffAdapter: StaffAdapter
    private val staffList = mutableListOf<Staff>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView and Adapter
        staffAdapter = StaffAdapter(staffList)
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.staffRecyclerView.adapter = staffAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
