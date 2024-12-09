package com.example.appque.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appque.databinding.FragmentRequestBinding
import com.example.appque.databinding.ItemRequestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val requestList = mutableListOf<Map<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        binding.requestRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.requestRecyclerView.adapter = RequestAdapter()

        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        try {
            binding.progressBar.visibility = View.VISIBLE

            database.child("pending_requests").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        requestList.clear()

                        for (child in snapshot.children) {
                            try {
                                val request = child.value as? MutableMap<String, Any>
                                if (request != null) {
                                    // Format the timestamp into a readable date and time
                                    val timestamp = request["timestamp"] as? Long
                                    if (timestamp != null) {
                                        val formattedDate = formatTimestamp(timestamp)
                                        request["formattedDate"] = formattedDate
                                    }
                                    requestList.add(request as Map<String, String>)
                                }
                            } catch (e: Exception) {
                                Log.e("PendingRequests", "Error parsing request data: ${e.message}")
                            }
                        }

                        binding.requestRecyclerView.adapter?.notifyDataSetChanged()

                        if (requestList.isEmpty()) {
                            binding.emptyListTextView.visibility = View.VISIBLE
                            binding.requestRecyclerView.visibility = View.GONE
                        } else {
                            binding.emptyListTextView.visibility = View.GONE
                            binding.requestRecyclerView.visibility = View.VISIBLE
                        }

                        binding.progressBar.visibility = View.GONE
                    } catch (e: Exception) {
                        Log.e("PendingRequests", "Error processing snapshot data: ${e.message}")
                        Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Log.e("PendingRequests", "Database error: ${error.message}")
                    Toast.makeText(context, "Failed to load requests: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Log.e("PendingRequests", "Unexpected error: ${e.message}")
            Toast.makeText(context, "An unexpected error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun deleteRequest(request: Map<String, String>, position: Int) {
        val email = request["email"] ?: return

        database.child("pending_requests")
            .orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                    requestList.removeAt(position)
                    binding.requestRecyclerView.adapter?.notifyItemRemoved(position)
                    Toast.makeText(context, "Request deleted successfully.", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error deleting request: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun acceptRequest(request: Map<String, String>, position: Int) {
        val email = request["email"] ?: return
        val password = request["password"] ?: return
        val id = request["id"] ?: return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                val timestamp = System.currentTimeMillis()

                val userMap = mapOf(
                    "id" to id,
                    "name" to request["name"],
                    "email" to email,
                    "course" to request["course"],
                    "year" to request["year"],
                    "role" to "student",
                    "uid" to userId,
                    "timestamp" to timestamp
                )
                database.child("users").child(userId).setValue(userMap)
                    .addOnSuccessListener {
                        removeRequestFromPending(email)
                        Toast.makeText(context, "User approved and added to users.", Toast.LENGTH_SHORT).show()
                        signOutNewUserAndRestoreAdmin()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOutNewUserAndRestoreAdmin() {
        val adminEmail = "admin@gmail.com"
        val adminPassword = "123456"

        auth.signOut()

        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
            .addOnSuccessListener {
                Toast.makeText(context, "Admin session restored.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to restore admin session: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeRequestFromPending(email: String) {
        database.child("pending_requests")
            .orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                    requestList.removeIf { it["email"] == email }
                    binding.requestRecyclerView.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error removing request: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    inner class RequestAdapter : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

        inner class RequestViewHolder(private val itemBinding: ItemRequestBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(request: Map<String, String>, position: Int) {
                itemBinding.nameTextView.text = "Name: ${request["name"]}"
                itemBinding.idTextView.text = "ID: ${request["id"]}"
                itemBinding.emailTextView.text = "Email: ${request["email"]}"
                itemBinding.courseTextView.text = "Course: ${request["course"]}"
                itemBinding.yearTextView.text = "Year: ${request["year"]}"
                itemBinding.timestampTextView.text = "Date & Time: ${request["formattedDate"] ?: "N/A"}"

                itemBinding.acceptButton.setOnClickListener {
                    showConfirmationDialog(
                        title = "Accept Request",
                        message = "Are you sure you want to accept this request?",
                        onConfirm = { acceptRequest(request, position) }
                    )
                }

                itemBinding.deleteButton.setOnClickListener {
                    showConfirmationDialog(
                        title = "Delete Request",
                        message = "Are you sure you want to delete this request?",
                        onConfirm = { deleteRequest(request, position) }
                    )
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemRequestBinding.inflate(inflater, parent, false)
            return RequestViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
            holder.bind(requestList[position], position)
        }

        override fun getItemCount(): Int = requestList.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
