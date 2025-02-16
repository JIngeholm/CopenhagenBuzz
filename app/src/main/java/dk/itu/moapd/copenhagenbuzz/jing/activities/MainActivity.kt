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

package dk.itu.moapd.copenhagenbuzz.jing.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main activity for the CopenhagenBuzz application.
 *
 * This activity handles event creation, including input for event details such as name, location,
 * date range, type, and description. It utilizes MaterialDatePicker for selecting event dates.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View binding instance for accessing UI components.
     */
    private lateinit var mainBinding: ActivityMainBinding

    private var isLoggedIn: Boolean = false

    /**
     * Companion object to store a static tag for logging.
     */
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    /**
     * Instance of [Event] used to store event details entered by the user.
     */
    private val event: Event = Event("", "", "", "", "")

    /**
     * Called when the activity is first created.
     * Initializes the UI and sets up event listeners.
     *
     * @param savedInstanceState A [Bundle] containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        initializeViews()

        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        invalidateOptionsMenu()
    }

    /**
     * Initializes the UI components and sets up event listeners for user interactions.
     */
    private fun initializeViews() {
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        with(mainBinding.contentMain) {

            // Top app bar handling______________________________________//

            setSupportActionBar(topAppBar)

            //___________________________________________________________//

            // Date picker handling______________________________________//

            editTextEventDateRange.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event start date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

                datePicker.show(supportFragmentManager, "DATE_PICKER")

                datePicker.addOnPositiveButtonClickListener { startDateMillis ->
                    val startDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date(startDateMillis))

                    val endDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select event end date")
                        .setSelection(startDateMillis)
                        .build()

                    endDatePicker.show(supportFragmentManager, "END_DATE_PICKER")

                    endDatePicker.addOnPositiveButtonClickListener { endDateMillis ->
                        val endDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date(endDateMillis))

                        val formattedDateRange = getString(R.string.event_date_range_format, startDate, endDate)
                        editTextEventDateRange.setText(formattedDateRange)
                    }
                }
            }

            //______________________________________________________________//

            // Event type drop down handling________________________________//

            val eventTypes = resources.getStringArray(R.array.event_types)

            val adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_dropdown_item_1line,
                eventTypes
            )

            mainBinding.contentMain.spinnerEventType.apply {
                setAdapter(adapter)
                isFocusable = false
                isFocusableInTouchMode = false
            }

            mainBinding.contentMain.spinnerEventType.setOnClickListener {
                mainBinding.contentMain.spinnerEventType.showDropDown()
            }

            //______________________________________________________________//

            // Add event handling___________________________________________//

            addEventButton.setOnClickListener {
                if (editTextEventName.text.toString().isNotEmpty() &&
                    editTextEventLocation.text.toString().isNotEmpty() &&
                    editTextEventDateRange.text.toString().isNotEmpty() &&
                    spinnerEventType.text.toString().isNotEmpty() &&
                    editTextEventDescription.text.toString().isNotEmpty()
                ) {
                    event.eventName = editTextEventName.text.toString().trim()
                    event.eventLocation = editTextEventLocation.text.toString().trim()
                    event.eventDate = editTextEventDateRange.text.toString().trim()
                    event.eventType = spinnerEventType.text.toString().trim()
                    event.eventDescription = editTextEventDescription.text.toString().trim()

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
            mainBinding.root,
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
