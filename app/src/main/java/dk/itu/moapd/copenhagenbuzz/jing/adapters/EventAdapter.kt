package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.data.Event

class EventAdapter(private val context: Context, private val events: ArrayList<Event>) : BaseAdapter() {

    // Inflater to inflate the list item layout
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // ViewHolder class to hold references to the views
    private class ViewHolder(view: View) {
        val eventNameTextView: TextView = view.findViewById(R.id.event_name)
        val eventTypeTextView: TextView = view.findViewById(R.id.event_type)
        val eventLocationTextView: TextView = view.findViewById(R.id.event_location)
        val eventDateTextView: TextView = view.findViewById(R.id.event_date)
        val eventPhotoImageView: ImageView = view.findViewById(R.id.event_photo)
        val circleTextView: TextView = view.findViewById(R.id.circle_text)
    }

    // Returns the number of items in the list
    override fun getCount(): Int {
        return events.size
    }

    // Returns the item at the specified position
    override fun getItem(position: Int): Event {
        return events[position]
    }

    // Returns the item ID at the specified position
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Returns the view for the specified position
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        // Check if the view is being reused
        if (convertView == null) {
            // Inflate the list item layout
            view = inflater.inflate(R.layout.event_row_item, parent, false)
            // Create a new ViewHolder
            viewHolder = ViewHolder(view)
            // Store the ViewHolder in the view's tag
            view.tag = viewHolder
        } else {
            // Reuse the existing view and ViewHolder
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
        viewHolder.circleTextView.text = event.eventType.first().toString()

        // Load the event photo using Glide
        Glide.with(context)
            .load(event.photo) // URL of the photo
            .placeholder(R.drawable.event_photo_placeholder) // Placeholder while loading
            .error(R.drawable.event_photo_placeholder) // Error image if loading fails
            .into(viewHolder.eventPhotoImageView)

        return view
    }
}