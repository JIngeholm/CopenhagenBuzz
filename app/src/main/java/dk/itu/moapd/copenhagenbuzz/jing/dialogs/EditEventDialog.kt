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

package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogEditEventBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditEventDialog : DialogFragment() {

    private var _binding: DialogEditEventBinding? = null
    private val binding get() = _binding!!
    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    // State constants
    companion object {
        private const val ARG_EVENT_ID = "event_id"
        private const val STATE_EVENT_NAME = "event_name"
        private const val STATE_EVENT_LOCATION = "event_location"
        private const val STATE_EVENT_DATE_RANGE = "event_date_range"
        private const val STATE_EVENT_TYPE = "event_type"
        private const val STATE_EVENT_DESCRIPTION = "event_description"
        private const val STATE_EVENT_PHOTO = "event_photo"

        fun newInstance(eventId: String): EditEventDialog {
            return EditEventDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_EVENT_ID, eventId)
                }
            }
        }
    }

    private var eventId: String = ""
    private lateinit var eventRef: DatabaseReference
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getString(ARG_EVENT_ID) ?: run {
            dismiss()
            return
        }
        eventRef = database.reference.child("events").child(eventId)
        event = Event("", "", "", "", "", "", "") // Initialize default event

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    binding.imageViewEventPicture.setImageURI(uri)
                    event.eventPhoto = uri.toString()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save all current values
        outState.putString(STATE_EVENT_NAME, binding.editTextEventName.text.toString())
        outState.putString(STATE_EVENT_LOCATION, binding.editTextEventLocation.text.toString())
        outState.putString(STATE_EVENT_DATE_RANGE, binding.editTextEventDateRange.text.toString())
        outState.putString(STATE_EVENT_TYPE, binding.spinnerEventType.text.toString())
        outState.putString(STATE_EVENT_DESCRIPTION, binding.editTextEventDescription.text.toString())
        outState.putString(STATE_EVENT_PHOTO, event.eventPhoto)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditEventBinding.inflate(layoutInflater)

        // Load fresh data from Firebase only if no saved state exists
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                event = snapshot.getValue(Event::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch event", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        })

        if (savedInstanceState != null) {
            // Restore from saved state
            binding.editTextEventName.setText(savedInstanceState.getString(STATE_EVENT_NAME))
            binding.editTextEventLocation.setText(savedInstanceState.getString(STATE_EVENT_LOCATION))
            binding.editTextEventDateRange.setText(savedInstanceState.getString(STATE_EVENT_DATE_RANGE))
            binding.spinnerEventType.setText(savedInstanceState.getString(STATE_EVENT_TYPE))
            binding.editTextEventDescription.setText(savedInstanceState.getString(STATE_EVENT_DESCRIPTION))

            val photoUri = savedInstanceState.getString(STATE_EVENT_PHOTO)
            if (!photoUri.isNullOrEmpty()) {
                event.eventPhoto = photoUri
                loadEventImage(photoUri)
            } else {
                binding.imageViewEventPicture.setImageResource(R.drawable.event_photo_placeholder)
            }
        } else {
            // Load fresh data from Firebase only if no saved state exists
            eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    event = snapshot.getValue(Event::class.java)!!
                    setInitialEventInfo(event)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch event", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            })
        }


        initializeViews()
        handleModButtons()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleModButtons() {
        binding.cancelEditButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            if (isInputValid()) {
                dataViewModel.updateEvent(getEditedEvent(event))
                Toast.makeText(requireContext(), "Event saved! 🎉", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }

    private fun getEditedEvent(event: Event): Event {
        val editedEvent = Event("", "", "", "", "", "", "")

        editedEvent.userId = event.userId
        editedEvent.eventID = event.eventID
        editedEvent.eventPhoto = event.eventPhoto
        editedEvent.favoritedBy = event.favoritedBy
        editedEvent.invitedUsers = event.invitedUsers

        editedEvent.eventName = binding.editTextEventName.text.toString().trim()
        editedEvent.eventLocation = binding.editTextEventLocation.text.toString().trim()
        editedEvent.eventStartDate = binding.editTextEventDateRange.text.toString().substringBefore(" to ").trim()
        editedEvent.eventEndDate = binding.editTextEventDateRange.text.toString().substringAfter(" to ").trim()
        editedEvent.eventType = binding.spinnerEventType.text.toString().trim()
        editedEvent.eventDescription = binding.editTextEventDescription.text.toString().trim()

        return editedEvent
    }

    private fun setInitialEventInfo(event: Event) {
        val formattedDate = "${event.eventStartDate} to ${event.eventEndDate}"

        binding.editTextEventName.setText(event.eventName)
        binding.editTextEventLocation.setText(event.eventLocation)
        binding.editTextEventDateRange.setText(formattedDate)
        binding.spinnerEventType.setText(event.eventType)
        binding.editTextEventDescription.setText(event.eventDescription)

        if (!event.eventPhoto.isNullOrEmpty()) {
            loadEventImage(event.eventPhoto)
        } else {
            binding.imageViewEventPicture.setImageResource(R.drawable.event_photo_placeholder)
        }
    }

    private fun loadEventImage(photoUri: String) {
        Picasso.get()
            .load(photoUri)
            .placeholder(R.drawable.baseline_image_not_supported_24)
            .error(R.drawable.event_photo_placeholder)
            .transform(object : Transformation {
                override fun transform(source: Bitmap): Bitmap {
                    val matrix = Matrix().apply { postRotate(90f) }
                    return Bitmap.createBitmap(
                        source, 0, 0, source.width, source.height, matrix, true
                    ).also { source.recycle() }
                }
                override fun key(): String = "rotate90"
            })
            .into(binding.imageViewEventPicture)
    }

    private fun initializeViews() {
        binding.editTextEventDateRange.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event start date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { startDateMillis ->
                val startDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(startDateMillis))
                event.eventStartDate = startDate

                val endDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event end date")
                    .setSelection(startDateMillis)
                    .build()

                endDatePicker.show(parentFragmentManager, "END_DATE_PICKER")

                endDatePicker.addOnPositiveButtonClickListener { endDateMillis ->
                    val endDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(endDateMillis))
                    event.eventEndDate = endDate
                    val formattedDateRange = getString(R.string.event_date_range_format, startDate, endDate)
                    binding.editTextEventDateRange.setText(formattedDateRange)
                }
            }
        }

        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        val eventTypes = resources.getStringArray(R.array.event_types)
        val adapter = ArrayAdapter(
            requireContext(),
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
    }

    private fun isInputValid(): Boolean {
        return binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDateRange.text.toString().isNotEmpty() &&
                binding.spinnerEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }
}