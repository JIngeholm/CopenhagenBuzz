package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.adapters.InviteAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogInviteBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.objects.EventLocation
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InviteDialog : DialogFragment() {

    private var _binding: DialogInviteBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()
    private val selectedUsers = mutableMapOf<String, String>()
    private val unInvitedUsers = mutableListOf<String>()
    private lateinit var event: Event

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventId: String): InviteDialog {
            return InviteDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_EVENT_ID, eventId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        event = Event("", EventLocation(), "", "", "", "", "") // Initialize default event
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogInviteBinding.inflate(layoutInflater)

        // Load the event data first
        database.reference.child("events").child(arguments?.getString(ARG_EVENT_ID) ?: "")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    event = snapshot.getValue(Event::class.java) ?: run {
                        dismiss()
                        return
                    }
                    setupRecyclerView()
                }

                override fun onCancelled(error: DatabaseError) {
                    dismiss()
                }
            })

        binding.saveButton.setOnClickListener {
            updateEventWithInvitedUsers()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupRecyclerView() {
        dataViewModel.auth.currentUser?.let {
            val query = database.reference
                .child("users")
                .orderByChild("username")

            val options = FirebaseRecyclerOptions.Builder<buzzUser>()
                .setQuery(query, buzzUser::class.java)
                .setLifecycleOwner(this)
                .build()

            val adapter = InviteAdapter(options, dataViewModel, event) { buzzUser, isChecked ->
                if (isChecked) {
                    selectedUsers[buzzUser.uid] = "Undecided"
                    if (unInvitedUsers.contains(buzzUser.uid)) {
                        unInvitedUsers.remove(buzzUser.uid)
                    }
                } else {
                    selectedUsers.remove(buzzUser.uid)
                    if (!unInvitedUsers.contains(buzzUser.uid)) {
                        unInvitedUsers.add(buzzUser.uid)
                    }
                }
            }

            binding.usersRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }
        }
    }

    private fun updateEventWithInvitedUsers() {
        val updatedInvitedUsers = event.invitedUsers.toMutableMap()

        selectedUsers.forEach { (uid, status) ->
            updatedInvitedUsers[uid] = status
        }

        unInvitedUsers.forEach { uid ->
            updatedInvitedUsers.remove(uid)
        }

        val updatedEvent = event.copy(
            invitedUsers = updatedInvitedUsers,
            eventName = event.eventName,
            eventLocation = event.eventLocation,
            eventStartDate = event.eventStartDate,
            eventEndDate = event.eventEndDate,
            eventType = event.eventType,
            eventDescription = event.eventDescription,
            eventPhoto = event.eventPhoto,
            userId = event.userId,
            eventID = event.eventID,
            favoritedBy = event.favoritedBy
        )

        dataViewModel.updateEvent(updatedEvent)
        dataViewModel.updateInvite(updatedEvent, unInvitedUsers)
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}