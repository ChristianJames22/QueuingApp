package com.example.appque

import Staff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class StaffAdapter(
    private val staffList: MutableList<Staff>,
    private val onItemClick: (Staff) -> Unit
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]
        holder.bind(staff)
    }

    override fun getItemCount(): Int = staffList.size

    inner class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(staff: Staff) {
            nameTextView.text = "Name: ${staff.name}"
            idTextView.text = "ID: ${staff.id}"
            emailTextView.text = "Email: ${staff.email}"

            // Format timestamp to a readable date and time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedTimestamp = dateFormat.format(staff.timestamp?.let { Date(it) })
            timestampTextView.text = "Added on: $formattedTimestamp"

            itemView.setOnClickListener {
                onItemClick(staff)
            }
        }
    }
}
