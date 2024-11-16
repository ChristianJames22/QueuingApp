package com.example.appque

import Staff // Ensure this is the correct model class for staff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StaffAdapter(private val staffList: MutableList<Staff>) :
    RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff, parent, false) // Ensure this layout exists
        return StaffViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]
        holder.nameTextView.text = "Name: ${staff.name}"
        holder.idTextView.text = "ID no.: ${staff.id}"
        holder.emailTextView.text = "Email: ${staff.email}"
    }

    override fun getItemCount() = staffList.size

    fun addStaff(staff: Staff) {
        staffList.add(staff)
        notifyItemInserted(staffList.size - 1)
    }

    class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
    }
}
