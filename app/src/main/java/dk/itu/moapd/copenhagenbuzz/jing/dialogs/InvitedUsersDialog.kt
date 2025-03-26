package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.adapters.InvitedUsersAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogInvitedUsersBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InvitedUsersDialog : DialogFragment() {

    private var _binding: DialogInvitedUsersBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()
    private val invitedUsersMap = mutableMapOf<buzzUser, String>()
    private lateinit var eventRef: DatabaseReference
    private lateinit var adapter: InvitedUsersAdapter
    private lateinit var event: Event

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventRef: DatabaseReference): InvitedUsersDialog {
            val args = Bundle().apply {
                putString(ARG_EVENT_ID, eventRef.key)
            }
            return InvitedUsersDialog().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventId = requireArguments().getString(ARG_EVENT_ID) ?: run {
            dismiss()
            return
        }
        // Initialize the correct event reference
        eventRef = database.reference.child("events").child(eventId)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogInvitedUsersBinding.inflate(layoutInflater)

        // First load the event data
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                event = snapshot.getValue(Event::class.java) ?: run {
                    Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show()
                    dismiss()
                    return
                }

                // Then load the invited users
                loadInvitedUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load event", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        })

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun loadInvitedUsers() {
        // This correctly points to the invitedUsers map within the specific event
        val invitedUsersRef = eventRef.child("invitedUsers")

        invitedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val invitedUsers = snapshot.getValue(object : GenericTypeIndicator<Map<String, String>>() {})
                invitedUsers?.let { fetchBuzzUsers(it) } ?: run {
                    Toast.makeText(context, "No invited users found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InvitedUsersDialog", "Failed to fetch invitedUsers map", error.toException())
            }
        })
    }

    private fun fetchBuzzUsers(invitedUsers: Map<String, String>) {
        val usersRef = database.reference.child("users")

        invitedUsers.forEach { (uid, status) ->
            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val buzzUser = snapshot.getValue(buzzUser::class.java)
                    buzzUser?.let {
                        invitedUsersMap[it] = status
                        if (invitedUsersMap.size == invitedUsers.size) {
                            setupRecyclerView()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InvitedUsersDialog", "Failed to fetch user: $uid", error.toException())
                }
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = InvitedUsersAdapter(invitedUsersMap, event, dataViewModel.auth.currentUser)
        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InvitedUsersDialog.adapter
        }
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