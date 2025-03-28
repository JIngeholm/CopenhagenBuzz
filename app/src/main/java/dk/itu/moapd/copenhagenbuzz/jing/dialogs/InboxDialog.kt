package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.adapters.InboxAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogInboxBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event

class InboxDialog : DialogFragment() {
    private var _binding: DialogInboxBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var adapter: InboxAdapter
    private val eventsList = mutableListOf<Event>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogInboxBinding.inflate(layoutInflater)

        dataViewModel.auth.currentUser?.let { user ->
            // Initialize adapter with empty list
            adapter = InboxAdapter(eventsList).apply {
                setOnAttendClickListener { event ->
                    handleResponse(event, "Attending")
                }
                setOnMaybeClickListener { event ->
                    handleResponse(event, "Maybe")
                }
                setOnDeclineClickListener { event ->
                    handleResponse(event, "Declined")
                }
            }

            binding.invitesRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = this@InboxDialog.adapter
            }

            fetchInvites(user.uid)

            binding.closeButton.setOnClickListener {
                dismiss()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun handleResponse(event: Event, response: String) {
        dataViewModel.auth.currentUser?.uid?.let { userId ->

            // Remove event from users invites
            database.reference
                .child("invites")
                .child(userId)
                .child(event.eventID)
                .removeValue()
                .addOnSuccessListener {
                    Log.d("InboxDialog", "Invite removed from invites")
                }
                .addOnFailureListener { e ->
                    Log.e("InboxDialog", "Failed to respond to invite", e)
                }

            // Update the user's status in the event's invitedUsers map
            database.reference
                .child("events")
                .child(event.eventID)
                .child("invitedUsers")
                .child(userId)  // The ID of the user responding
                .setValue(response)  // "attending", "maybe", or "declined"
                .addOnSuccessListener {
                    Log.d("InboxDialog", "Updated invitedUsers status for $userId")
                }
                .addOnFailureListener { e ->
                    Log.e("InboxDialog", "Failed to update invitedUsers status", e)
                }
        }
    }

    private fun fetchInvites(userId: String) {
        val invitesRef = database.reference
            .child("invites")
            .child(userId)

        invitesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val eventIds = snapshot.children.mapNotNull { it.key }
                    fetchEventDetails(eventIds)
                } else {
                    Log.d("InboxDialog", "No invites found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InboxDialog", "Failed to fetch invites", error.toException())
            }
        })
    }

    private fun fetchEventDetails(eventIds: List<String>) {
        val eventsRef = database.reference
            .child("events")

        eventIds.forEach { eventId ->
            eventsRef.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(eventSnapshot: DataSnapshot) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    event?.let {
                        eventsList.add(it)
                        adapter.notifyDataSetChanged() // Notify adapter of data change
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InboxDialog", "Failed to fetch event $eventId", error.toException())
                }
            })
        }
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