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

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FavoriteRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

/**
 * A RecyclerView adapter that displays a list of favorite events using FirebaseRecyclerAdapter.
 * This adapter binds event data to the UI and loads event images using Picasso.
 *
 * @property options The FirebaseRecyclerOptions that provide the query for Firebase data.
 * @property dataViewModel The DataViewModel used for managing data, particularly for handling favorites.
 */
class FavoriteAdapter(options: FirebaseRecyclerOptions<Event>, private val dataViewModel: DataViewModel) : FirebaseRecyclerAdapter<Event, FavoriteAdapter.ViewHolder>(options) {

    /**
     * A ViewHolder that represents an individual item in the RecyclerView.
     *
     * @property binding The view binding for the favorite row item layout.
     */
    inner class ViewHolder(private val binding: FavoriteRowItemBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the event data to the views in the ViewHolder.
         *
         * @param event The event object containing the event details.
         */
        fun bind(event: Event) {
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
            binding.circleIcon.setImageResource(iconResource)

            // Load the event photo using Picasso with a 90-degree rotation
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

            // Remove event from favorites
            binding.like.setOnClickListener {
                dataViewModel.toggleFavorite(event, true)
            }
        }
    }

    /**
     * Creates and returns a new ViewHolder instance.
     *
     * @param parent The parent ViewGroup that will contain the ViewHolder.
     * @param viewType The type of the view (unused in this case).
     * @return A new ViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FavoriteRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Binds the ViewHolder with the event data at the given position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     * @param model The event model containing data.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Event) {
        Log.d("TimeLineAdapter", "Populating item at position: $position")
        holder.bind(model)
    }
}
