/*
MIT License

Copyright (c) [2025] [Johan Ingeholm]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

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

    inner class ViewHolder(
        private val binding: InvitedUserRowItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

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
            setupStatusClickListener(user)
        }

        private fun setupStatusClickListener(user: buzzUser) {
            val isCurrentUser = user.uid == currentUser?.uid

            binding.inviteStatusIcon.apply {
                setOnClickListener {
                    if (isCurrentUser) handleStatusChange()
                }
                isClickable = isCurrentUser
                isFocusable = isCurrentUser
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
            val (textRes, iconRes) = when (status) {
                "Maybe" -> Pair("Maybe", R.drawable.baseline_maybe_24)
                "Not Attending" -> Pair("Not Attending", R.drawable.baseline_cancel_24)
                "Attending" -> Pair("Attending", R.drawable.baseline_check_circle_24)
                else -> Pair("Invited", R.drawable.baseline_undecided_24)
            }

            binding.inviteStatus.text = textRes
            binding.inviteStatusIcon.setBackgroundResource(iconRes)
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
                removeInviteFromUserNode(userId)
            }
            .addOnFailureListener { e ->
                Log.e("InvitedUsersAdapter", "Failed to update status", e)
            }
    }

    private fun removeInviteFromUserNode(userId: String) {
        Firebase.database(DATABASE_URL).reference
            .child("invites")
            .child(userId)
            .child(event.eventID)
            .removeValue()
            .addOnSuccessListener {
                Log.d("InvitedUsersAdapter", "Removed invite for $userId")
            }
    }
}