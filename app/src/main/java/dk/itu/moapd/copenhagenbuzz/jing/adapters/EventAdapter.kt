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

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

/**
 * Adapter class for displaying a list of events in a ListView.
 *
 * This adapter extends [BaseAdapter] and is responsible for binding event data
 * to the list item views.
 *
 * @param context The context used for inflating layouts and loading resources.
 * @param events The list of [Event] objects to be displayed.
 */
class EventAdapter(private val context: Context, private val dataViewModel: DataViewModel, private val events: ArrayList<Event>) : BaseAdapter() {

    // Inflater to inflate the list item layout
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * ViewHolder class to hold references to the views within a list item.
     * This improves performance by avoiding repeated calls to findViewById.
     *
     * @param view The root view of the list item.
     */
    private class ViewHolder(view: View) {
        val eventNameTextView: TextView = view.findViewById(R.id.event_name)
        val eventTypeTextView: TextView = view.findViewById(R.id.event_type)
        val eventLocationTextView: TextView = view.findViewById(R.id.event_location)
        val eventDateTextView: TextView = view.findViewById(R.id.event_date)
        val eventDescriptionTextView: TextView = view.findViewById(R.id.event_description)
        val eventPhotoImageView: ImageView = view.findViewById(R.id.event_photo)
        val circleTextView: TextView = view.findViewById(R.id.circle_text)
        val likeButton: Button = view.findViewById(R.id.like)
    }

    /**
     * Returns the number of items in the list.
     * @return The total number of events.
     */
    override fun getCount(): Int {
        return events.size
    }

    /**
     * Returns the event at the specified position.
     * @param position The index of the event.
     * @return The [Event] object at the given position.
     */
    override fun getItem(position: Int): Event {
        return events[position]
    }

    /**
     * Returns the item ID at the specified position.
     * @param position The index of the item.
     * @return The item ID as a [Long].
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Returns the view for the specified position in the list.
     *
     * This method reuses existing views for better performance, using the ViewHolder pattern.
     *
     * @param position The index of the item.
     * @param convertView The recycled view, or null if a new view should be created.
     * @param parent The parent ViewGroup.
     * @return The populated [View] representing the event.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            // Inflate the list item layout
            view = inflater.inflate(R.layout.event_row_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder // Store ViewHolder in view's tag
        } else {
            // Reuse existing view and ViewHolder
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Get the event at the current position
        val event = getItem(position)

        // Bind the event data to the views
        viewHolder.eventNameTextView.text = event.eventName
        viewHolder.eventTypeTextView.text = event.eventType
        viewHolder.eventLocationTextView.text = event.eventLocation
        viewHolder.eventDateTextView.text = event.eventDate
        viewHolder.eventDescriptionTextView.text = event.eventDescription
        viewHolder.circleTextView.text = event.eventType.first().toString()

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
            .into(viewHolder.eventPhotoImageView) // Use the ImageView from the ViewHolder

        // Set the like button background based on the 'liked' state
        if (event.liked) {
            viewHolder.likeButton.setBackgroundResource(R.drawable.baseline_favorite_24)
        } else {
            viewHolder.likeButton.setBackgroundResource(R.drawable.baseline_favorite_border_24)
        }

        viewHolder.likeButton.setOnClickListener{
            // Toggle the liked state
            dataViewModel.toggleFavorite(event)

            if (event.liked) {
                viewHolder.likeButton.setBackgroundResource(R.drawable.baseline_favorite_24)
            } else {
                viewHolder.likeButton.setBackgroundResource(R.drawable.baseline_favorite_border_24)
            }

            // Notify the adapter that only this item has changed
            notifyDataSetChanged()
        }

        return view
    }


    /**
     * Updates the adapter's data with a new list of events.
     *
     * This method clears the existing events in the adapter and adds the new list of events.
     * After updating the data, it notifies the adapter that the data set has changed,
     * ensuring the list view is refreshed to reflect the new data.
     *
     * @param newEvents A list of new events that will replace the current events in the adapter.
     */
    fun updateData(newEvents: List<Event>) {
        this.events.clear()          // Clear the existing events in the adapter
        this.events.addAll(newEvents)  // Add the new events to the adapter
        notifyDataSetChanged()       // Notify the adapter that the data has changed and the view should be updated
    }


}
