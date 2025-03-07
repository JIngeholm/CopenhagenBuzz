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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FavoriteRowItemBinding

/**
 * Adapter for displaying a list of favorite events in a RecyclerView.
 *
 * This adapter binds event data to the corresponding views in each item of the RecyclerView.
 *
 * @param data The list of favorite events to display.
 */
class FavoriteAdapter(private val data: ArrayList<Event>) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    /**
     * ViewHolder class to hold references to views in each item of the RecyclerView.
     *
     * @property binding The binding object for the row item layout.
     */
    inner class ViewHolder(private val binding: FavoriteRowItemBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the event data to the views in the item layout.
         *
         * @param event The event data to bind.
         */
        fun bind(event: Event) {
            // Binding the event data to the views
            binding.eventName.text = event.eventName
            binding.eventType.text = event.eventType

            // Use Picasso to load the event photo with a 90-degree rotation
            Picasso.get()
                .load(event.eventPhoto) // URL or resource ID of the photo
                .placeholder(R.drawable.baseline_image_not_supported_24) // Placeholder image while loading
                .error(R.drawable.event_photo_placeholder) // Error image if loading fails
                .transform(object : com.squareup.picasso.Transformation {
                    override fun transform(source: Bitmap): Bitmap {
                        // Rotate the image 90 degrees to the right (clockwise)
                        val matrix = android.graphics.Matrix()
                        matrix.postRotate(90f)
                        val rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
                        source.recycle() // Recycle the original bitmap to prevent memory leaks
                        return rotatedBitmap
                    }

                    override fun key(): String {
                        return "rotate90"
                    }
                })
                .into(binding.eventPhoto) // Use the ImageView from the ViewHolder

            // Set the first character of the event type to the circleText
            binding.circleText.text = event.eventType.firstOrNull()?.toString()
        }
    }

    /**
     * Creates a new ViewHolder by inflating the item layout.
     *
     * @param parent The parent view that the new view will be attached to.
     * @param viewType The type of view to create.
     * @return The created ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FavoriteRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Binds the data for a specific position to the corresponding ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the data in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    /**
     * Returns the number of items in the data set.
     *
     * @return The number of items in the data set.
     */
    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Updates the adapter's data and notifies the RecyclerView of changes.
     *
     * @param newData The new list of favorite events.
     */
    fun updateData(newData: ArrayList<Event>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}
