package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.jing.adapters.InviteAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogInviteBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InviteDialog(private val event: Event) : DialogFragment() {

    private var _binding: DialogInviteBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()

    // Map to store selected users (UID -> Status)
    private val selectedUsers = mutableMapOf<String, String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogInviteBinding.inflate(layoutInflater)

        dataViewModel.auth.currentUser?.let {
            // Query to fetch all users
            val query = Firebase.database(DATABASE_URL).reference
                .child("users")
                .orderByChild("username")

            val options = FirebaseRecyclerOptions.Builder<buzzUser>()
                .setQuery(query, buzzUser::class.java)
                .setLifecycleOwner(this)
                .build()

            // Create the adapter with a callback for checkbox state changes
            val adapter = InviteAdapter(options, dataViewModel, event) { buzzUser, isChecked ->
                if (isChecked) {
                    // Add user to the map with status "Undecided"
                    selectedUsers[buzzUser.uid] = "Undecided"
                } else {
                    // Remove user from the map
                    selectedUsers.remove(buzzUser.uid)
                }
            }

            binding.usersRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }

            // Set a click listener for the "Invite" button
            binding.saveButton.setOnClickListener {
                updateEventWithInvitedUsers()
            }

            binding.cancelButton.setOnClickListener {
                dismiss()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun updateEventWithInvitedUsers() {
        // Create a mutable copy of the existing invited users
        val updatedInvitedUsers = event.invitedUsers.toMutableMap()

        // Add or update UIDs of selected users (checked users)
        selectedUsers.forEach { (uid, status) ->
            updatedInvitedUsers[uid] = status
        }

        // Remove UIDs of users not in selectedUsers (unchecked users)
        updatedInvitedUsers.keys.removeAll { uid ->
            !selectedUsers.containsKey(uid)
        }

        // Create a copy of the event with the updated map of invited users
        val updatedEvent = event.copy(invitedUsers = updatedInvitedUsers)

        // Call the editEvent method in the ViewModel
        dataViewModel.editEvent(updatedEvent)

        // Dismiss the dialog
        dismiss()
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}