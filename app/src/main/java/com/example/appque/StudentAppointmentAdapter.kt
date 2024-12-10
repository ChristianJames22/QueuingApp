package com.example.appque

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appque.databinding.ItemCashierappointmentBinding

class StudentAppointmentAdapter : RecyclerView.Adapter<StudentAppointmentAdapter.AppointmentViewHolder>() {

    // Mutable list to hold the appointments
    private val appointments = mutableListOf<String>()

    // Method to update the list of appointments
    fun submitList(newAppointments: List<String>) {
        appointments.clear()
        appointments.addAll(newAppointments)
        notifyDataSetChanged() // Notify the adapter about data changes
    }

    inner class AppointmentViewHolder(private val binding: ItemCashierappointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: String) {
            binding.queueAppointment.text = appointment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemCashierappointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size
}
