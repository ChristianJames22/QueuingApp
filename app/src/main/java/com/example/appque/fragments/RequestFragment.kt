package com.example.appque.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var secondaryAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        val requestList = mutableListOf<Map<String, String>>() // Holds pending user requests
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        initializeSecondaryAuth()
        database = FirebaseDatabase.getInstance().reference

        // Set up RecyclerView
        binding.requestRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.requestRecyclerView.adapter = RequestAdapter()

        // Fetch requests from Firebase
        fetchRequestsFromFirebase()
    }

    private fun initializeSecondaryAuth() {
        try {
            val secondaryApp = FirebaseApp.getInstance("SecondaryApp")
            secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
        } catch (e: IllegalStateException) {
            val secondaryApp = FirebaseApp.initializeApp(
                requireContext(),
                FirebaseApp.getInstance().options,
                "SecondaryApp"
            )
            secondaryAuth = FirebaseAuth.getInstance(secondaryApp!!)
        }
    }

    private fun fetchRequestsFromFirebase() {
        showLoading(true)
        database.child("temp_requests")
            .addValueEventListener(object : ValueEventListener {
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
                    Toast.makeText(context, "Error fetching requests. Try again.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateEmptyState() {
        if (requestList.isEmpty()) {
            binding.emptyListTextView.visibility = View.VISIBLE
            binding.requestRecyclerView.visibility = View.GONE
        } else {
            binding.emptyListTextView.visibility = View.GONE
            binding.requestRecyclerView.visibility = View.VISIBLE
        }
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

        val adminEmail = sharedPreferences.getString("admin_email", null)
        val adminPassword = sharedPreferences.getString("admin_password", null)

        if (adminEmail == null || adminPassword == null) {
            showToast("Admin credentials not found. Please log in again.")
            return
        }

        // Use secondaryAuth to create the user
        secondaryAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        val user = mapOf(
                            "id" to id,
                            "name" to name,
                            "course" to course,
                            "year" to year,
                            "email" to email,
                            "role" to "student"
                        )
                        database.child("users").child(userId).setValue(user)
                            .addOnSuccessListener {
                                removeRequestFromFirebase(request)
                                secondaryAuth.signOut() // Log out the newly created user

                                // Re-authenticate the admin
                                auth.signInWithEmailAndPassword(adminEmail, adminPassword)
                                    .addOnCompleteListener { loginTask ->
                                        if (loginTask.isSuccessful) {
                                            showToast("Request accepted. Admin session restored.")
                                        } else {
                                            showToast("Failed to restore admin session.")
                                        }
                                        showLoading(false)
                                    }
                            }
                            .addOnFailureListener { e ->
                                showToast("Failed to save user: ${e.message}")
                                showLoading(false)
                            }
                    }
                } else {
                    showToast("Error creating user: ${task.exception?.message}")
                    showLoading(false)
                }
            }
    }

    private fun deleteRequest(position: Int) {
        val request = requestList[position]
        removeRequestFromFirebase(request)
    }

    private fun removeRequestFromFirebase(request: Map<String, String>) {
        database.child("temp_requests")
            .orderByChild("email")
            .equalTo(request["email"])
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                    removeRequestFromList(request)
                    showToast("Request deleted.")
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error deleting request: ${error.message}")
                }
            })
    }

    private fun removeRequestFromList(request: Map<String, String>) {
        val position = requestList.indexOf(request)
        if (position >= 0) {
            requestList.removeAt(position)
            binding.requestRecyclerView.adapter?.notifyItemRemoved(position)
            updateEmptyState()
        }
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
