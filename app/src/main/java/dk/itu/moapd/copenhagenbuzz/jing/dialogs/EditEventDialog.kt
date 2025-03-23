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
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogEditEventBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A dialog fragment for editing an existing event.
 *
 * This dialog allows users to modify event details such as name, location,
 * date range, type, description, and event photo. Changes can be saved or canceled.
 *
 * @property event The event to be edited.
 */
class EditEventDialog(val event: Event) : DialogFragment() {

    private var _binding: DialogEditEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private val dataViewModel: DataViewModel by activityViewModels()

    /**
     * Creates and returns the dialog for editing an event.
     *
     * Initializes the UI components, sets event information, and handles user interactions.
     *
     * @param savedInstanceState The saved instance state bundle.
     * @return The created dialog.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditEventBinding.inflate(layoutInflater)

        // Initialize ActivityResultLauncher for image picking
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                imageUri?.let {
                    binding.imageViewEventPicture.setImageURI(it)
                    event.eventPhoto = it.toString() // Update the event's photo
                }
            }
        }

        setInitialEventInfo(event)
        initializeViews()
        handleModButtons()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    /**
     * Adjusts the dialog dimensions when it starts.
     */
    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    /**
     * Cleans up resources when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Handles modification buttons (save and cancel).
     *
     * - The cancel button dismisses the dialog.
     * - The save button validates input, updates the event, and saves changes.
     */
    fun handleModButtons() {
        binding.cancelEditButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            if (isInputValid()) {
                dataViewModel.editEvent(getEditedEvent(event))
                Toast.makeText(requireContext(), "Event saved! 🎉", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }

    /**
     * Retrieves the modified event data from input fields.
     *
     * @param event The original event to be updated.
     * @return A new [Event] object with updated details.
     */
    fun getEditedEvent(event: Event): Event {
        val editedEvent = Event("", "", "", "", "", "", "")

        editedEvent.userId = event.userId
        editedEvent.eventID = event.eventID
        editedEvent.eventPhoto = event.eventPhoto

        editedEvent.eventName = binding.editTextEventName.text.toString().trim()
        editedEvent.eventLocation = binding.editTextEventLocation.text.toString().trim()
        editedEvent.eventStartDate = binding.editTextEventDateRange.text.toString().substringBefore(" to ").trim()
        editedEvent.eventEndDate = binding.editTextEventDateRange.text.toString().substringAfter(" to ").trim()
        editedEvent.eventType = binding.spinnerEventType.text.toString().trim()
        editedEvent.eventDescription = binding.editTextEventDescription.text.toString().trim()

        return editedEvent
    }

    /**
     * Populates the input fields with the event's existing data.
     *
     * @param event The event whose data is displayed.
     */
    fun setInitialEventInfo(event: Event) {
        val formattedDate = "${event.eventStartDate} to ${event.eventEndDate}"

        binding.editTextEventName.setText(event.eventName)
        binding.editTextEventLocation.setText(event.eventLocation)
        binding.editTextEventDateRange.setText(formattedDate)
        binding.spinnerEventType.setText(event.eventType)
        binding.editTextEventDescription.setText(event.eventDescription)

        // Load the event photo using Picasso with a 90-degree rotation
        Picasso.get()
            .load(event.eventPhoto)
            .placeholder(R.drawable.baseline_image_not_supported_24)
            .error(R.drawable.event_photo_placeholder)
            .transform(object : Transformation {
                override fun transform(source: Bitmap): Bitmap {
                    val matrix = Matrix().apply { postRotate(90f) }
                    val rotatedBitmap = Bitmap.createBitmap(
                        source, 0, 0, source.width, source.height, matrix, true
                    )
                    source.recycle() // Prevent memory leaks
                    return rotatedBitmap
                }

                override fun key(): String = "rotate90"
            })
            .into(binding.imageViewEventPicture)
    }

    /**
     * Initializes view elements such as date picker, image picker, and event type dropdown.
     */
    fun initializeViews() {
        // Set up date range picker
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

        // Set up image selection button
        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // Set up spinner for event types
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

        // Open dropdown when the spinner is clicked
        binding.spinnerEventType.setOnClickListener {
            binding.spinnerEventType.showDropDown()
        }
    }

    /**
     * Validates whether all required input fields are filled.
     *
     * @return `true` if all fields contain valid data, `false` otherwise.
     */
    private fun isInputValid(): Boolean {
        return binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDateRange.text.toString().isNotEmpty() &&
                binding.spinnerEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }
}
