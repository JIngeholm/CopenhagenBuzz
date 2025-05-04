package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event

/**
 * RecyclerView adapter for rendering a month‑grid calendar.
 *
 * Each cell represents a day of the month (or an empty spacer if day == 0),
 * and displays an event indicator dot when there are one or more [Event]s
 * on that day.  Taps are forwarded to [OnDayClickListener].
 *
 * @param days       A list of Ints representing each grid cell:
 *                   • `0` for empty/spacer cells
 *                   • `1..N` for actual days of the month
 * @param events     A map from day‑of‑month → list of [Event]s on that day.
 * @param listener   Listener to handle clicks on valid day‑cells.
 */
class CalendarAdapter(
    private var days: List<Int>,
    private var events: Map<Int, List<Event>>,
    private val listener: OnDayClickListener
) : RecyclerView.Adapter<CalendarAdapter.CalenderViewHolder>() {

    /**
     * Callback interface for handling taps on calendar day cells.
     */
    interface OnDayClickListener {
        /**
         * Called when the user taps a valid day cell.
         *
         * @param day    The 1‑based day of month that was tapped.
         * @param events The list of events on that day, or null/empty if none.
         */
        fun onDayClicked(day: Int, events: List<Event>?)
    }

    /**
     * ViewHolder for a single calendar day cell.
     *
     * @param itemView The inflated [View] for this cell.
     */
    inner class CalenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.day_text)
        val eventIndicator: View = itemView.findViewById(R.id.event_indicator)
        val dayCell: CardView = itemView.findViewById(R.id.day_cell)
    }

    /**
     * Inflates the [R.layout.calendar_day_item] and wraps it in a [CalenderViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalenderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_item, parent, false)
        return CalenderViewHolder(view)
    }

    /**
     * Binds data for a single calendar cell:
     * - If `day > 0`, shows the day number, toggles the event indicator,
     *   and sets up the click listener.
     * - If `day == 0`, clears text/indicator and removes click handling.
     *
     * @param holder   The [CalenderViewHolder] to bind.
     * @param position Index into the [days] list.
     */
    override fun onBindViewHolder(holder: CalenderViewHolder, position: Int) {
        val day = days[position]

        // Configure cell appearance based on if it's a valid day
        if (day > 0) {
            // Valid day in current month
            holder.dayText.text = "$day"
            holder.dayText.alpha = 1.0f

            // Check if there are any events for this day
            val eventsForDay = events[day]
            holder.eventIndicator.visibility =
                if (!eventsForDay.isNullOrEmpty()) View.VISIBLE else View.GONE

            holder.dayCell.setOnClickListener {
                listener.onDayClicked(day, eventsForDay)

            }
        } else {
            // Empty cell or day from another month
            holder.dayText.text = ""
            holder.eventIndicator.visibility = View.INVISIBLE
            holder.dayCell.setOnClickListener(null)
        }
    }

    /**
     * @return The total number of grid cells (including spacers).
     */
    override fun getItemCount(): Int = days.size

    /**
     * Updates the adapter’s data and refreshes the view.
     *
     * @param newDays   New list of day‑values (0 = spacer, 1..N = days).
     * @param newEvents New map of day → events on that day.
     */
    fun updateDays(newDays: List<Int>, newEvents: Map<Int, List<Event>>) {
        days = newDays
        events = newEvents
        notifyDataSetChanged()
    }
}