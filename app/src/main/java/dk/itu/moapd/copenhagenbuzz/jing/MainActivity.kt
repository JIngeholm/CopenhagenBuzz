package dk.itu.moapd.copenhagenbuzz.jing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.widget.EditText
import android.util.Log
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dk.itu.moapd.copenhagenbuzz.jing.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

     private lateinit var binding: ActivityMainBinding

     companion object {
         private val TAG = MainActivity::class.qualifiedName
     }

    private lateinit var eventName: EditText
    private lateinit var eventLocation: EditText
    private lateinit var eventDate: TextInputEditText
    private lateinit var eventType: EditText
    private lateinit var eventDescription: EditText

    private lateinit var addEventButton: FloatingActionButton

    private val event: Event = Event("", "","","", "")

    override fun onCreate(savedInstanceState: Bundle?) {
         WindowCompat.setDecorFitsSystemWindows(window , false)
         super.onCreate(savedInstanceState)

         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)

        eventName = findViewById(R.id.edit_text_event_name)
        eventLocation = findViewById(R.id.edit_text_event_location)
        eventDate = findViewById(R.id.edit_text_event_date_range)
        eventType = findViewById(R.id.edit_text_event_type)
        eventDescription = findViewById(R.id.edit_text_event_description)

        addEventButton = findViewById(R.id.addEventButton)


         eventDate.setOnClickListener {
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

                     eventDate.setText(formattedDateRange)
                 }
             }
         }

        addEventButton.setOnClickListener {
            if(eventName.text.toString().isNotEmpty() && eventLocation.text.toString().isNotEmpty()
                && eventDate.text.toString().isNotEmpty() && eventType.text.toString().isNotEmpty()
                && eventDescription.text.toString().isNotEmpty()){

                event.setEventName(eventName.text.toString().trim())
                event.setEventLocation(eventLocation.text.toString().trim())
                event.setEventDate(eventDate.text.toString().trim())
                event.setEventType(eventType.text.toString().trim())
                event.setEventDescription(eventDescription.text.toString().trim())

                showMessage()
            }
        }

     }

    private fun showMessage(){
        Log.d(TAG, event.toString())
    }

}