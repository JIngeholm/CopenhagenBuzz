package dk.itu.moapd.copenhagenbuzz.jing.adapters

import DataViewModel
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
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.EventRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.DeleteEventDialog
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.EditEventDialog

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

    private fun bindEvent(binding: EventRowItemBinding, event: Event) {
        binding.eventName.text = event.eventName
        binding.eventType.text = event.eventType
        binding.eventLocation.text = event.eventLocation
        binding.eventDate.text = event.eventDate
        binding.eventDescription.text = event.eventDescription
        binding.circleText.text = event.eventType.firstOrNull()?.toString() ?: ""

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

        like(binding,event)

        editOrDelete(binding,event)
    }



    fun like(binding: EventRowItemBinding, event: Event){
        // Query the favorites table for the current user
        val favoritesRef = Firebase.database(DATABASE_URL).reference
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

    fun editOrDelete(binding: EventRowItemBinding, event: Event){
        if (event.userId != dataViewModel.auth.uid){
            binding.modButtons.isVisible = false
        }

        binding.deleteButton.setOnClickListener{
            val deleteEventDialog = DeleteEventDialog(event)
            deleteEventDialog.show(fragmentManager, "DeleteEventDialog")
        }

        binding.editButton.setOnClickListener{
            val editEventDialog = EditEventDialog(event)
            editEventDialog.show(fragmentManager, "EditEventDialog")
        }
    }
}