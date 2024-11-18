package com.example.appque.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appque.MainActivity
import com.example.appque.R
import com.google.firebase.auth.FirebaseAuth

class RequestFragment : Fragment(R.layout.fragment_request) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the settings button
        val settingsButton: ImageButton = view.findViewById(R.id.settingsButton)

        // Set up the click listener for the settings menu
        settingsButton.setOnClickListener {
            showSettingsMenu(it)
        }
    }

    private fun showSettingsMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut() // Clear Firebase session
                navigateToLogin()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
