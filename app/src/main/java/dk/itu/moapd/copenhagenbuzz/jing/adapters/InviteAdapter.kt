package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.UserRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InviteAdapter(
    options: FirebaseRecyclerOptions<buzzUser>,
    dataViewModel: DataViewModel,
    private val event: Event, // Pass the event object
    private val onUserChecked: (buzzUser, Boolean) -> Unit // Callback for checkbox state changes
) : FirebaseRecyclerAdapter<buzzUser, InviteAdapter.ViewHolder>(options) {

    private val currentUserUid = dataViewModel.auth.currentUser?.uid

    inner class ViewHolder(private val binding: UserRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: buzzUser, isChecked: Boolean) {
            binding.Name.text = user.username

            Picasso.get()
                .load(user.profilePicture)
                .placeholder(R.drawable.baseline_image_not_supported_24)
                .error(R.drawable.event_photo_placeholder)
                .into(binding.profilePicture)

            // Set the checkbox state
            binding.checkbox.isChecked = isChecked

            // Set a listener for the checkbox
            binding.checkbox.setOnCheckedChangeListener { _, isCheckedNew ->
                onUserChecked(user, isCheckedNew) // No more shadowing
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: buzzUser) {
        // Check if the user is already in the event's invitedUsers list
        val isChecked = event.invitedUsers.contains(model.uid)

        Log.d("InviteAdapter", "Populating item at position: $position")
        holder.bind(model, isChecked)
    }

    override fun getItemCount(): Int {
        // Exclude the current user from the item count
        return super.getItemCount() - countCurrentUser()
    }

    override fun getItem(position: Int): buzzUser {
        // Adjust the position to skip the current user
        var adjustedPosition = position
        for (i in 0..position) {
            if (super.getItem(i).uid == currentUserUid) {
                adjustedPosition++
            }
        }
        return super.getItem(adjustedPosition)
    }

    private fun countCurrentUser(): Int {
        // Count how many times the current user appears in the data set
        var count = 0
        for (i in 0 until super.getItemCount()) {
            if (super.getItem(i).uid == currentUserUid) {
                count++
            }
        }
        return count
    }
}