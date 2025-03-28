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

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.EventRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.DeleteEventDialog
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.EditEventDialog
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.InviteDialog
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.InvitedUsersDialog
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

class TimeLineAdapter(options: FirebaseListOptions<Event>, private val dataViewModel: DataViewModel, private val fragmentManager: FragmentManager) : FirebaseListAdapter<Event>(options) {

    override fun populateView(view: View, event: Event, position: Int) {
        // Get the binding from the view's tag
        val binding = view.tag as EventRowItemBinding

        // Bind the event data to the view
        bindEvent(binding, event)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate the layout for each item in the ListView
        val binding: EventRowItemBinding
        if (convertView == null) {
            binding = EventRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.root.tag = binding
        } else {
            binding = convertView.tag as EventRowItemBinding
        }

        // Get the event at the current position
        val event = getItem(position)

        // Bind the event data to the view
        bindEvent(binding, event)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun bindEvent(binding: EventRowItemBinding, event: Event) {
        val formattedDate = event.eventStartDate + " to " + event.eventEndDate

        // Set different icons based on event type
        val iconResource = when (event.eventType) {
            "Dinner" -> R.drawable.baseline_dinner_dining_24
            "Lunch" -> R.drawable.baseline_lunch_dining_24
            "Party" -> R.drawable.baseline_celebration_24
            "Sport" -> R.drawable.baseline_sports_basketball_24
            "Music" -> R.drawable.baseline_music_note_24
            "Art" -> R.drawable.baseline_brush_24
            "School" -> R.drawable.baseline_school_24
            else -> R.drawable.baseline_celebration_24
        }

        binding.eventName.text = event.eventName
        binding.eventType.text = event.eventType
        binding.eventLocation.text = event.eventLocation.address
        binding.eventDate.text = formattedDate
        binding.eventDescription.text = event.eventDescription
        binding.circleIcon.setImageResource(iconResource)

        // Load the event photo using Picasso with a 90-degree rotation
        if(event.eventPhoto == "") event.eventPhoto = R.drawable.event_photo_placeholder.toString()

        Picasso.get()
            .load(event.eventPhoto)
            .placeholder(R.drawable.baseline_image_not_supported_24)
            .error(R.drawable.event_photo_placeholder)
            .transform(object : Transformation {
                override fun transform(source: Bitmap): Bitmap {
                    val matrix = Matrix().apply { postRotate(90f) }
                    val rotatedBitmap = Bitmap.createBitmap(
                        source, 0, 0, source.width, source.height, matrix, true
                    )
                    source.recycle() // Prevent memory leaks
                    return rotatedBitmap
                }

                override fun key(): String = "rotate90"
            })
            .into(binding.eventPhoto)

        if(!event.invitedUsers.containsKey(dataViewModel.auth.uid) && event.userId != dataViewModel.auth.currentUser?.uid){
            binding.invited.isVisible = false
        }

        var invitedUsersListSize = event.invitedUsers.size.toString()

        binding.invited.text = "$invitedUsersListSize invited"

        like(binding,event)

        editDeleteOrInvite(binding,event)
    }



    private fun like(binding: EventRowItemBinding, event: Event){
        // Query the favorites table for the current user
        val favoritesRef = database.reference
            .child("favorites")
            .child(dataViewModel.auth.uid ?: "")

        // Check if the event is in the favorites table
        favoritesRef.child(event.eventID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Set the 'liked' state based on whether the event exists in favorites
                var liked = dataSnapshot.exists()

                // Set the like button click listener
                binding.like.setOnClickListener {
                    // Pass the 'liked' state to the toggleFavorite function
                    liked = dataViewModel.toggleFavorite(event, liked)
                    changeLikeIcon(liked)
                }

                changeLikeIcon(liked)
            }

            fun changeLikeIcon(liked: Boolean){
                if(dataViewModel.auth.currentUser?.isAnonymous == true) binding.like.isVisible = false

                // Update the like button background based on the 'liked' state
                if (liked) {
                    binding.like.setBackgroundResource(R.drawable.baseline_favorite_24)
                } else {
                    binding.like.setBackgroundResource(R.drawable.baseline_favorite_border_24)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log the error and optionally notify the user
                Log.e("FirebaseError", "Failed to check favorites: ${error.message}")

                // Optionally, show a message to the user
                Toast.makeText(
                    binding.root.context,
                    "Failed to check favorites: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun editDeleteOrInvite(binding: EventRowItemBinding, event: Event){
        if (event.userId != dataViewModel.auth.uid){
            binding.modButtons.isVisible = false
        }

        binding.inviteButton.setOnClickListener{
            InviteDialog.newInstance(event.eventID).show(
                fragmentManager,
                "InviteDialog"
            )
        }

        binding.deleteButton.setOnClickListener {
            DeleteEventDialog.newInstance(event.eventID).show(
                fragmentManager,
                "DeleteEventDialog"
            )
        }

        binding.editButton.setOnClickListener {
            EditEventDialog.newInstance(event.eventID).show(
                fragmentManager,
                "EditEventDialog"
            )
        }

        binding.invited.setOnClickListener {
            val eventRef = database.reference.child("events").child(event.eventID)
            InvitedUsersDialog.newInstance(eventRef).show(fragmentManager, "InvitedUsersDialog")
        }
    }
}