package dk.itu.moapd.copenhagenbuzz.jing.fragments

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.activities.LoginActivity
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentAddEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

"bundle"

class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null

    private var isLoggedIn: Boolean = false

    private val event: Event = Event("", "", "", "", "")

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentAddEventBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews()

        invalidateOptionsMenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Initializes the UI components and sets up event listeners for user interactions.
     */
    private fun initializeViews() {

        // Background handling_______________________________________//

        val isDarkMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        // Set the overlay visibility based on dark mode
        if (isDarkMode) {
            binding.darkModeOverlay.visibility = android.view.View.VISIBLE
        } else {
            binding.darkModeOverlay.visibility = android.view.View.GONE
        }

        //___________________________________________________________//

        // Top app bar handling______________________________________//

        binding.setSupportActionBar(topAppBar)

        //___________________________________________________________//

        // Date picker handling______________________________________//

        binding.editTextEventDateRange.setOnClickListener {
            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event start date")
                .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { startDateMillis ->
                val startDate = SimpleDateFormat("MM-dd-yyyy", java.util.Locale.getDefault()).format(
                    Date(startDateMillis)
                )

                val endDatePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event end date")
                    .setSelection(startDateMillis)
                    .build()

                endDatePicker.show(supportFragmentManager, "END_DATE_PICKER")

                endDatePicker.addOnPositiveButtonClickListener { endDateMillis ->
                    val endDate = SimpleDateFormat("MM-dd-yyyy", java.util.Locale.getDefault()).format(
                        Date(endDateMillis)
                    )
                    val formattedDateRange = getString(dk.itu.moapd.copenhagenbuzz.jing.R.string.event_date_range_format, startDate, endDate)
                    binding.editTextEventDateRange.setText(formattedDateRange)
                }
            }
        }

        //______________________________________________________________//

        // Event type drop down handling________________________________//

        val eventTypes = resources.getStringArray(dk.itu.moapd.copenhagenbuzz.jing.R.array.event_types)

        val adapter = ArrayAdapter(
            this@AddEventFragment,
            android.R.layout.simple_dropdown_item_1line,
            eventTypes
        )


        binding.spinnerEventType.apply {
            setAdapter(adapter)
            isFocusable = false
            isFocusableInTouchMode = false
        }

        binding.spinnerEventType.setOnClickListener {
            binding.spinnerEventType.showDropDown()
        }

        //______________________________________________________________//

        // Add event handling___________________________________________//

        binding.addEventButton.setOnClickListener {
            if (binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDateRange.text.toString().isNotEmpty() &&
                binding.spinnerEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
            ) {
                event.eventName = binding.editTextEventName.text.toString().trim()
                event.eventLocation = binding.editTextEventLocation.text.toString().trim()
                event.eventDate = binding.editTextEventDateRange.text.toString().trim()
                event.eventType = binding.spinnerEventType.text.toString().trim()
                event.eventDescription = binding.editTextEventDescription.text.toString().trim()

                showMessage()
            }
        }

        //______________________________________________________________//
        }
    }

    /**
     * Displays a fun Snackbar with the event parameters.
     */
    private fun showMessage() {
        val eventDetails = """
        🎉 *New Event Created!* 🎊
        📛 Name: ${event.eventName}
        📍 Location: ${event.eventLocation}
        📅 Date: ${event.eventDate}
        🔖 Type: ${event.eventType}
        📝 Description: ${event.eventDescription}
    """.trimIndent()

        val snack = Snackbar.make(
            binding.root,
            eventDetails,
            Snackbar.LENGTH_LONG
        )

        // Increase duration
        snack.setDuration(15000)

        // Access and customize the Snackbar's TextView
        val snackView = snack.view
        val textView = snackView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        // Customize text appearance
        textView.textSize = 16f
        textView.maxLines = 10 // Allow up to 10 lines (adjust as needed)
        textView.setTextColor(resources.getColor(R.color.white)) // Set text color
        textView.setPadding(32, 16, 32, 16) // Optional padding for better visuals

        snack.setAction("🎟️ Share") {
            Toast.makeText(this, "Event shared! 🎉", Toast.LENGTH_SHORT).show()
        }

        snack.show()
    }




    /**
     * Prepares the options menu by inflating the menu layout and updating
     * the visibility of login and logout menu items based on the user's login status.
     *
     * @param menu The options menu in which items are placed.
     * @return Boolean value indicating whether the menu should be displayed.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)

        menu.findItem(R.id.action_login).isVisible = !isLoggedIn
        menu.findItem(R.id.action_logout).isVisible = isLoggedIn
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Handles menu item selections. Navigates to the login activity if the login
     * or logout action is selected. Clears the back stack on logout to prevent
     * navigation back to the main activity.
     *
     * @param item The menu item that was selected.
     * @return Boolean value indicating whether the item selection was handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.action_logout -> {
                isLoggedIn = false
                invalidateOptionsMenu()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}