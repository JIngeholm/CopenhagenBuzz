package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.database
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.InvitedUserRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InvitedUsersAdapter(
    private val invitedUsersMap: MutableMap<buzzUser, String>,
    private val event: Event,
    private val currentUser: FirebaseUser?
) : RecyclerView.Adapter<InvitedUsersAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: InvitedUserRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentStatus: String = "Unknown"
        private lateinit var currentBuzzUser: buzzUser

        fun bind(user: buzzUser, status: String) {
            currentBuzzUser = user
            currentStatus = status
            binding.Name.text = user.username

            Picasso.get()
                .load(user.profilePicture)
                .placeholder(R.drawable.guest_24)
                .error(R.drawable.guest_24)
                .into(binding.profilePicture)

            updateStatusUI(status)

            // Only set click listener if this is the current user
            if (user.uid == currentUser?.uid) {
                binding.inviteStatusIcon.setOnClickListener {
                    handleStatusChange()
                }
                binding.inviteStatusIcon.isClickable = true
            } else {
                binding.inviteStatusIcon.setOnClickListener(null)
                binding.inviteStatusIcon.isClickable = false
                binding.inviteStatusIcon.isFocusable = false
            }
        }

        private fun handleStatusChange() {
            val newStatus = when (currentStatus) {
                "Maybe" -> "Not Attending"
                "Not Attending" -> "Attending"
                "Attending" -> "Maybe"
                "Undecided" -> "Attending"
                else -> "Attending"
            }

            currentStatus = newStatus
            updateStatusUI(newStatus)
            invitedUsersMap[currentBuzzUser] = newStatus
            updateInviteStatusInFirebase(currentBuzzUser.uid, newStatus)
        }

        private fun updateStatusUI(status: String) {
            when (status) {
                "Maybe" -> {
                    binding.inviteStatus.text = "Maybe"
                    binding.inviteStatusIcon.setBackgroundResource(R.drawable.baseline_maybe_24)
                }
                "Not Attending" -> {
                    binding.inviteStatus.text = "Not Attending"
                    binding.inviteStatusIcon.setBackgroundResource(R.drawable.baseline_cancel_24)
                }
                "Attending" -> {
                    binding.inviteStatus.text = "Attending"
                    binding.inviteStatusIcon.setBackgroundResource(R.drawable.baseline_check_circle_24)
                }
                else -> {
                    binding.inviteStatus.text = "Invited"
                    binding.inviteStatusIcon.setBackgroundResource(R.drawable.baseline_undecided_24)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InvitedUserRowItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = invitedUsersMap.keys.elementAt(position)
        val status = invitedUsersMap[user] ?: "Unknown"
        holder.bind(user, status)
    }

    override fun getItemCount(): Int = invitedUsersMap.size

    private fun updateInviteStatusInFirebase(userId: String, newStatus: String) {
        val eventInviteRef = Firebase.database(DATABASE_URL).reference
            .child("events")
            .child(event.eventID)
            .child("invitedUsers")
            .child(userId)

        eventInviteRef.setValue(newStatus)
            .addOnSuccessListener {
                Log.d("InvitedUsersAdapter", "Updated status for user $userId to $newStatus")

                Firebase.database(DATABASE_URL).reference
                    .child("invites")
                    .child(userId)
                    .child(event.eventID)
                    .removeValue()
                    .addOnSuccessListener {
                        Log.d("InvitedUsersAdapter", "Removed invite for $userId")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("InvitedUsersAdapter", "Failed to update status", e)
            }
    }
}