package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.jing.adapters.InvitedUsersAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogInvitedUsersBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InvitedUsersDialog(private val event: Event) : DialogFragment() {

    private var _binding: DialogInvitedUsersBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()

    // Map to store the final data (buzzUser -> invite status)
    private val invitedUsersMap = mutableMapOf<buzzUser, String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogInvitedUsersBinding.inflate(layoutInflater)

        dataViewModel.auth.currentUser?.let {
            // Step 1: Query the invitedUsers map from the event
            val invitedUsersRef = Firebase.database(DATABASE_URL).reference
                .child("events")
                .child(event.eventID)
                .child("invitedUsers")

            invitedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Step 2: Extract the UIDs and invite statuses from the map
                    val invitedUsers = snapshot.getValue(object : GenericTypeIndicator<Map<String, String>>() {})
                    if (invitedUsers != null) {
                        // Step 3: Query the users node to retrieve buzzUser objects
                        fetchBuzzUsers(invitedUsers)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InvitedUsersDialog", "Failed to fetch invitedUsers map", error.toException())
                }
            })

            binding.closeButton.setOnClickListener{
                dismiss()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun fetchBuzzUsers(invitedUsers: Map<String, String>) {
        val usersRef = Firebase.database(DATABASE_URL).reference.child("users")

        // Step 4: Query the users node for each UID in the invitedUsers map
        invitedUsers.forEach { (uid, status) ->
            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val buzzUser = snapshot.getValue(buzzUser::class.java)
                    if (buzzUser != null) {
                        // Step 5: Replace the UID with the corresponding buzzUser object
                        invitedUsersMap[buzzUser] = status

                        // Step 6: Update the RecyclerView adapter when all users are fetched
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
        // Create the adapter with the final map (buzzUser -> invite status)
        val adapter = InvitedUsersAdapter(invitedUsersMap)

        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
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