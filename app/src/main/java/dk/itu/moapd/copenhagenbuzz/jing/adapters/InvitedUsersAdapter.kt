package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.InvitedUserRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InvitedUsersAdapter(
    private val invitedUsersMap: Map<buzzUser, String> // Map of buzzUser -> invite status
) : RecyclerView.Adapter<InvitedUsersAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: InvitedUserRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: buzzUser, status: String) {
            binding.Name.text = user.username

            Picasso.get()
                .load(user.profilePicture)
                .placeholder(R.drawable.baseline_image_not_supported_24)
                .error(R.drawable.event_photo_placeholder)
                .into(binding.profilePicture)

            // Update the invite status and icon based on the status
            when (status) {
                "Maybe" -> {
                    binding.inviteStatus.text = "Maybe"
                    binding.inviteStatusIcon.setImageResource(R.drawable.baseline_maybe_24)
                }
                "Not Attending" -> {
                    binding.inviteStatus.text = "Not Attending"
                    binding.inviteStatusIcon.setImageResource(R.drawable.baseline_cancel_24)
                }
                "Attending" -> {
                    binding.inviteStatus.text = "Attending"
                    binding.inviteStatusIcon.setImageResource(R.drawable.baseline_check_circle_24)
                }
                else -> {
                    binding.inviteStatus.text = "Invited"
                    binding.inviteStatusIcon.setImageResource(R.drawable.baseline_undecided_24)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InvitedUserRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the user and status at the current position
        val user = invitedUsersMap.keys.elementAt(position)
        val status = invitedUsersMap[user] ?: "Unknown"

        Log.d("InvitedUsersAdapter", "Populating item at position: $position")
        holder.bind(user, status)
    }

    override fun getItemCount(): Int {
        return invitedUsersMap.size
    }
}