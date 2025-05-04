package dk.itu.moapd.copenhagenbuzz.jing.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.adapters.CalendarAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentCalendarBinding
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.EventDetailsDialog
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A [Fragment] displaying a month‑grid calendar, with events overlaid on each day.
 *
 * All date parsing and formatting is done in this fragment using a single,
 * strict format “MMMM d, yyyy” (e.g. “May 4, 2025”). Any deviation will fail to parse.
 *
 * Implements [CalendarAdapter.OnDayClickListener] to open:
 *  • [EventDetailsDialog] for a single event
 *  • [DayEventsDialog] for multiple events on the same day
 */
class CalendarFragment : Fragment(), CalendarAdapter.OnDayClickListener {

    private val TAG = "calendarFragment"

    private var _binding: FragmentCalendarBinding? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()
    private var currentMonthEvents = mutableMapOf<Int, List<Event>>()

    companion object {
        /** The strict date format used throughout this fragment. */
        private val STRICT_FORMAT = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).apply {
            isLenient = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCalendarBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarHeader()
        setupCalenderGrid()
        setupNavigationButtons()

        /*
        // Recompute when the events list changes
        dataViewModel.events.observe(viewLifecycleOwner) { events: List<Event> ->
            updateEventsForCurrentMonth(events)
        }

         */

        val query = database.reference.child("events").orderByChild("eventName")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    if (event != null) {
                        events.add(event)
                    }
                }
                // Now update the current month or do whatever you need with the filtered events
                updateEventsForCurrentMonth(events)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read events", error.toException())
            }
        })
    }

    /**
     * Initializes the calendar header text.
     */
    private fun setupCalendarHeader() {
        updateMonthYearText()
    }

    /**
     * Sets up the RecyclerView grid and its adapter.
     */
    private fun setupCalenderGrid() {
        val daysPerWeek = 7
        binding.calendarGrid.layoutManager = GridLayoutManager(requireContext(), daysPerWeek)

        // Create adapter with current month data
        calendarAdapter = CalendarAdapter(getDaysInMonth(), currentMonthEvents, this)
        binding.calendarGrid.adapter = calendarAdapter
    }

    /**
     * Configures the “Previous” and “Next” month buttons.
     */
    private fun setupNavigationButtons() {
        binding.prevMonthButton.setOnClickListener { _: View ->
            calendar.add(Calendar.MONTH, -1)
            updateCalenderView()
        }

        binding.nextMonthButton.setOnClickListener { _: View ->
            calendar.add(Calendar.MONTH, 1)
            updateCalenderView()
        }
    }

    /**
     * Refreshes header and events when the month changes.
     */
    private fun updateCalenderView() {
        updateMonthYearText()

        val query = database.reference
            .child("events")
            .orderByChild("eventStartDate")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events: List<Event> = snapshot.children.mapNotNull { it.getValue(Event::class.java) }

                updateEventsForCurrentMonth(events)
                calendarAdapter.updateDays(getDaysInMonth(), currentMonthEvents)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch events", error.toException())
            }
        })
    }


    /**
     * Updates the header text to “MMMM yyyy” (e.g. “May 2025”),
     * by formatting the [calendar] month.
     */
    private fun updateMonthYearText() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYearText = dateFormat.format(calendar.time)
        binding.textMonthYear.text = monthYearText
    }

    /**
     * Builds a list of day‑cell values:
     *  • `0` for empty leading cells
     *  • `1..maxDay` for each actual day
     *
     * @return List of Ints representing the grid cells.
     */
    private fun getDaysInMonth(): List<Int> {
        val daysInMonth = mutableListOf<Int>()

        // Set calender to the first day of the month
        val tempCalender = calendar.clone() as Calendar
        tempCalender.set(Calendar.DAY_OF_MONTH, 1)

        // Get the day of the week for the first day (Sunday is 0)
        val firstDayOfWeek = tempCalender.get(Calendar.DAY_OF_WEEK) - 1

        // Add empty spaces for days before the first day of the month
        for (i in 0 until firstDayOfWeek) {
            daysInMonth.add(0) // 0 represents an empty space
        }

        // Add the actual days of the month
        val daysCount = tempCalender.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysCount) {
            daysInMonth.add(i)
        }

        return daysInMonth
    }

    /**
     * Filters the provided [events] to only those whose start date
     * (parsed from their stored string) falls in the displayed month/year.
     * Then groups them by day‑of‑month into [currentMonthEvents].
     *
     * @param events Full list of all events from the ViewModel.
     */
    private fun updateEventsForCurrentMonth(events: List<Event>) {
        currentMonthEvents.clear()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        // Keep only events in this month/year
        events.filterNotNull().forEach { e ->
            parseEventDate(e.eventStartDate)?.let { cal : Calendar ->
                if (cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year) {
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    val listForDay = currentMonthEvents[day].orEmpty()
                    currentMonthEvents[day] = listForDay + e
                }
            }
        }

        calendarAdapter.updateDays(getDaysInMonth(), currentMonthEvents)
    }

    /**
     * Parses a date string in “MMMM d, yyyy” format into a [Calendar].
     *
     * @param dateString e.g. “May 4, 2025”
     * @return Calendar on success, or null if parsing failed.
     */
    private fun parseEventDate(dateString: String): Calendar? {
        if (dateString.isBlank()) return null
        return try {
            val date = STRICT_FORMAT.parse(dateString)
            Calendar.getInstance().apply { time = date!! }
        } catch (e: ParseException) {
            Log.e(TAG, "Unable to parse date: $dateString", e)
            null
        }
    }


    /**
     * Called by [CalendarAdapter] when the user taps a day cell.
     *
     * @param day 1‑based day of month (or ≤0 for empty cells)
     * @param events List of events on that day, if any
     */
    override fun onDayClicked(day: Int, events: List<Event>?) {
        if (day <= 0 || events.isNullOrEmpty()) return

        // Get current month and year
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        if (events.size == 1) {
            // If there's only one event, show its details directly
            val dialog = EventDetailsDialog.newInstance(events.first().eventID)
            dialog.show(parentFragmentManager, "EventDetailsDialog")
        } else {
            // If there are multiple events, show the events list dialog
            val eventIds = events.map { event: Event -> event.eventID }
            val dialog = DayEventsDialog.newInstance(day, month, year, eventIds)
            dialog.show(parentFragmentManager, "DayEventsDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}