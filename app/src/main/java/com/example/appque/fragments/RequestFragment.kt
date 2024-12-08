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

class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var secondaryAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    companion object {
        val requestList = mutableListOf<Map<String, String>>() // Holds pending user requests
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        secondaryAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Set up RecyclerView
        binding.requestRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.requestRecyclerView.adapter = RequestAdapter()

        // Fetch requests from Firebase
        fetchRequestsFromFirebase()
    }

    private fun fetchRequestsFromFirebase() {
        showLoading(true)
        database.child("temp_requests").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()
                for (child in snapshot.children) {
                    val request = child.value as? Map<String, String>
                    if (request != null) {
                        requestList.add(request)
                    }
                }
                binding.requestRecyclerView.adapter?.notifyDataSetChanged()
                updateEmptyState()
                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Log.e("RequestFragment", "Error fetching requests: ${error.message}")
                showToast("Error fetching requests. Try again.")
            }
        })
    }

    private fun updateEmptyState() {
        binding.emptyListTextView.visibility = if (requestList.isEmpty()) View.VISIBLE else View.GONE
        binding.requestRecyclerView.visibility = if (requestList.isEmpty()) View.GONE else View.VISIBLE
    }

    inner class RequestAdapter : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

        inner class RequestViewHolder(private val itemBinding: ItemRequestBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(request: Map<String, String>) {
                itemBinding.nameTextView.text = "Name: ${request["name"]}"
                itemBinding.idTextView.text = "ID: ${request["id"]}"
                itemBinding.emailTextView.text = "Email: ${request["email"]}"
                itemBinding.courseTextView.text = "Course: ${request["course"]}"
                itemBinding.yearTextView.text = "Year: ${request["year"]}"

                itemBinding.acceptButton.setOnClickListener {
                    showConfirmationDialog("Accept Request", "Are you sure you want to accept this request?") {
                        showLoading(true)
                        acceptRequest(request)
                    }
                }

                itemBinding.deleteButton.setOnClickListener {
                    showConfirmationDialog("Delete Request", "Are you sure you want to delete this request?") {
                        showLoading(true)
                        deleteRequest(adapterPosition)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemRequestBinding.inflate(inflater, parent, false)
            return RequestViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
            holder.bind(requestList[position])
        }

        override fun getItemCount(): Int = requestList.size
    }

    private fun acceptRequest(request: Map<String, String>) {
        val email = request["email"] ?: return
        val password = request["password"] ?: return
        val id = request["id"] ?: return
        val name = request["name"] ?: return
        val course = request["course"] ?: return
        val year = request["year"] ?: return

        val userMap = hashMapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "course" to course,
            "year" to year,
            "role" to "student", // Default role
            "timestamp" to System.currentTimeMillis()
        )

        showLoading(true)

        // Push user data to a "pending_users" node for processing
        database.child("temp_request").push().setValue(userMap)
            .addOnSuccessListener {
                // Remove request after successful push
                removeRequestFromFirebase(email)
                showToast("Request accepted. User will be created shortly.")
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("RequestFragment", "Error accepting request: ${e.message}")
                showToast("Failed to accept request: ${e.message}")
            }
    }


    private fun deleteRequest(position: Int) {
        val request = requestList[position]
        val email = request["email"] ?: return
        removeRequestFromFirebase(email)
    }

    private fun removeRequestFromFirebase(email: String) {
        database.child("temp_requests")
            .orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                    requestList.removeIf { it["email"] == email }
                    binding.requestRecyclerView.adapter?.notifyDataSetChanged()
                    updateEmptyState()
                    showToast("Request successfully deleted.")
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error deleting request: ${error.message}")
                }
            })
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.requestRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
