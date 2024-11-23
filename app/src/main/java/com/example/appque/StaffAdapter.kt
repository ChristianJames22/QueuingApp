package com.example.appque

import Staff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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

    override fun getItemCount(): Int {
        return staffList.size
    }

    /**
     * Optional: Method to update the data and refresh the RecyclerView
     */
    fun updateData(newList: List<Staff>) {
        staffList.clear()
        staffList.addAll(newList)
        notifyDataSetChanged() // Refresh the entire RecyclerView
    }

    inner class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)

        fun bind(staff: Staff) {
            // Use string resources or String.format() for better localization
            nameTextView.text = itemView.context.getString(R.string.staff_name, staff.name)
            idTextView.text = itemView.context.getString(R.string.staff_id, staff.id)
            emailTextView.text = itemView.context.getString(R.string.staff_email, staff.email)

            // Handle click events
            itemView.setOnClickListener {
                onItemClick(staff)
            }
        }
    }
}
