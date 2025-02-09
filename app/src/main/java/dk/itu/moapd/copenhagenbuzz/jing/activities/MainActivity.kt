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

import android.os.Bundle
import android.util.Log
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
    }

    /**
     * Initializes the UI components and sets up event listeners for user interactions.
     */
    private fun initializeViews() {
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        with(mainBinding.contentMain) {

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

            addEventButton.setOnClickListener {
                if (editTextEventName.text.toString().isNotEmpty() &&
                    editTextEventLocation.text.toString().isNotEmpty() &&
                    editTextEventDateRange.text.toString().isNotEmpty() &&
                    editTextEventType.text.toString().isNotEmpty() &&
                    editTextEventDescription.text.toString().isNotEmpty()
                ) {
                    event.eventName = editTextEventName.text.toString().trim()
                    event.eventLocation = editTextEventLocation.text.toString().trim()
                    event.eventDate = editTextEventDateRange.text.toString().trim()
                    event.eventType = editTextEventType.text.toString().trim()
                    event.eventDescription = editTextEventDescription.text.toString().trim()

                    showMessage()
                }
            }
        }
    }

    /**
     * Displays a Snackbar with the event parameters
     */
    private fun showMessage() {
        val snack = Snackbar.make(
            mainBinding.root,
            "Event added using \n${event.toString()}",
            Snackbar.LENGTH_LONG
        )

        snack.setDuration(10000)
        snack.show()
    }
}
