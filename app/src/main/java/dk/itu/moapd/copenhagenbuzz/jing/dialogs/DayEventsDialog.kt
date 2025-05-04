package dk.itu.moapd.copenhagenbuzz.jing.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.EventDetailsDialog
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import java.text.SimpleDateFormat
import java.util.*

/**
 * DialogFragment that shows all events for a selected calendar day.
 *
 * Displays the date in the title and a scrollable list of events
 * (with name, type, photo, and favorite toggle) for that day.
 */
class DayEventsDialog : DialogFragment() {

    private lateinit var date: Calendar

    private val dataViewModel: DataViewModel by activityViewModels()

    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_MONTH = "month"
        private const val ARG_YEAR = "year"
        private const val ARG_EVENT_IDS = "event_ids"

        /**
         * Creates a new instance of the dialog with specified events and date.
         *
         * @param day Day of month
         * @param month Month (0-11)
         * @param year Year
         * @param eventIds List of event IDs for this day
         * @return A new instance of [DayEventsDialogFragment]
         */
        fun newInstance(
            day: Int,
            month: Int,
            year: Int,
            eventIds: List<String>
        ): DayEventsDialog {
            val fragment = DayEventsDialog()
            fragment.arguments = Bundle().apply {
                putInt(ARG_DAY, day)
                putInt(ARG_MONTH, month)
                putInt(ARG_YEAR, year)
                putStringArrayList(ARG_EVENT_IDS, ArrayList(eventIds))
            }
            return fragment
        }
    }

    /**
     * Builds and returns the dialog UI.
     *
     * - Reads the date and event IDs from arguments
     * - Formats and sets the dialog title to "Events for {MMMM d, yyyy}"
     * - Configures a RecyclerView with [EventListAdapter]
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_day_events, null)

        // Get arguments
        val day = arguments?.getInt(ARG_DAY) ?: 1
        val month = arguments?.getInt(ARG_MONTH) ?: 0
        val year = arguments?.getInt(ARG_YEAR) ?: 2025
        val eventIds = arguments?.getStringArrayList(ARG_EVENT_IDS) ?: arrayListOf()

        // Create date
        date = Calendar.getInstance()
        date.set(year, month, day)

        // Format the date as "dd-MM-yyyy"
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = sdf.format(date.time)

        // Set the dialog title with formatted date
        val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
        dialogTitle.text = getString(R.string.events_for_day, formattedDate)


        // Setup RecyclerView to list events
        val recyclerView = view.findViewById<RecyclerView>(R.id.events_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val eventList = mutableListOf<Event>()

        val reference = database.reference.child("events")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val event = child.getValue(Event::class.java)
                    if (event != null && eventIds.contains(event.eventID)) {
                        eventList.add(event)
                    }
                }

                recyclerView.adapter =
                    EventListAdapter(eventList, dataViewModel, parentFragmentManager)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading events", error.toException())
            }
        })


        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    /**
     * Adapter for rendering a vertical list of events in the dialog.
     *
     * @param eventIds        IDs of the events to display
     * @param dataViewModel   ViewModel providing event and favorite data
     * @param fragmentManager FragmentManager for launching detail dialogs
     */
    inner class EventListAdapter(
        private val events: List<Event>,
        private val dataViewModel: DataViewModel,
        private val fragmentManager: FragmentManager
    ) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

        /**
         * ViewHolder representing one event row: name, type, photo, favorite toggle.
         */
        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val eventName: TextView = itemView.findViewById(R.id.event_name)
            val eventType: TextView = itemView.findViewById(R.id.event_type)
            val eventPhoto: ImageView = itemView.findViewById(R.id.event_photo)
            val circleIcon: ImageView = itemView.findViewById(R.id.circle_icon)
        }

        /**
         * Inflates the layout for a single event row.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.calendar_row_item, parent, false)
            return EventViewHolder(view)
        }

        /**
         * Binds an event's data into the ViewHolder:
         * - Sets name, type, and photo
         * - Displays filled or outlined heart based on favorite status
         * - Handles favorite toggle clicks
         * - Launches [EventDetailsDialogFragment] on row click
         */
        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val event = events[position]

            holder.eventName.text = event.eventName
            holder.eventType.text = event.eventType

            if (event.eventPhoto.isNotEmpty()) {
                Picasso.get()
                    .load(event.eventPhoto)
                    .placeholder(R.drawable.baseline_refresh_24)
                    .error(R.drawable.baseline_image_not_supported_24)
                    .into(holder.eventPhoto)
            } else {
                holder.eventPhoto.setImageResource(R.drawable.baseline_image_not_supported_24)
            }

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

            holder.circleIcon.setImageResource(iconResource)

            holder.itemView.setOnClickListener {
                EventDetailsDialog
                    .newInstance(event.eventID)
                    .show(fragmentManager, "EventDetailsDialog")
            }
        }

        /**
         * @return Number of events (rows) in this list.
         */
        override fun getItemCount(): Int = events.size
    }
}