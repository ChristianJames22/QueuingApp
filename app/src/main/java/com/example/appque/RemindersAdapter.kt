package com.example.appque.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appque.R
import com.example.appque.fragments.Reminder

class RemindersAdapter(
    private val remindersList: List<Reminder>,
    private val onReminderClick: (Reminder) -> Unit
) : RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.RemindersTextView) // For reminder title
        val timeTextView: TextView = view.findViewById(R.id.TimeTextView) // For reminder time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminders, parent, false) // Updated layout file
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = remindersList[position]
        holder.titleTextView.text = reminder.title // Set reminder title
        holder.timeTextView.text = reminder.time // Set reminder time
        holder.itemView.setOnClickListener {
            onReminderClick(reminder)
        }
    }

    override fun getItemCount(): Int = remindersList.size
}
