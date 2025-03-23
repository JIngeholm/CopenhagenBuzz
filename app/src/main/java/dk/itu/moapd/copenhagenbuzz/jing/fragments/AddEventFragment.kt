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

package dk.itu.moapd.copenhagenbuzz.jing.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentAddEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

/**
 * Fragment responsible for adding a new event.
 *
 * This fragment provides an interface for users to create and add events to the system.
 * It includes fields for the event's name, location, date range, type, description, and an option to select an image.
 * Once the event details are filled out, users can save the event, which will be added to the ViewModel and reflected in the system.
 *
 * The fragment also handles date range selection and image picking for the event.
 */
class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null

    private val event: Event = Event("", "", "", "", "", "", "")
    private val dataViewModel: DataViewModel by activityViewModels()

    companion object {
        private const val IMAGE_REQUEST_CODE = 100
    }

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    /**
     * Inflates the fragment's layout and sets up the binding.
     *
     * This method initializes the view by inflating the layout and binding it to the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate the view.
     * @param container The parent container that the fragment's UI will be attached to.
     * @param savedInstanceState A bundle containing saved state information, if available.
     * @return The root view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)

        // Initialize ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                // Handle the selected image URI (e.g., display it in an ImageView)
                imageUri?.let {
                    binding.imageViewEventPicture.setImageURI(it)
                    event.eventPhoto = it.toString() // Update the event's photo
                }
            }
        }
        return binding.root
    }

    /**
     * Initializes the UI components and sets up event listeners for user interactions.
     *
     * This method is responsible for initializing the views, setting up date range pickers,
     * image selection, event type selection, and handling the Add Event button click.
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState A bundle containing saved state information, if available.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        event.userId = dataViewModel.auth.currentUser?.uid ?: "unknown user"

        initializeViews()
    }

    /**
     * Cleans up the binding when the view is destroyed.
     *
     * This method is called when the view is destroyed. It releases the binding to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Initializes UI components and sets their listeners.
     *
     * This method configures the UI components, including setting up the date range pickers,
     * image selection, event type spinner, and the Add Event button's click listener.
     */
    private fun initializeViews() {

        // Set up date range picker for event start and end dates
        binding.editTextEventDateRange.setOnClickListener {
            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event start date")
                .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { startDateMillis ->
                val startDate = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(
                    Date(startDateMillis)
                )

                event.eventStartDate = startDate

                // Set up end date picker after selecting start date
                val endDatePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event end date")
                    .setSelection(startDateMillis)
                    .build()

                endDatePicker.show(parentFragmentManager, "END_DATE_PICKER")

                endDatePicker.addOnPositiveButtonClickListener { endDateMillis ->
                    val endDate = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(
                        Date(endDateMillis)
                    )

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

        // Set up the Add Event button click listener
        binding.addEventButton.setOnClickListener {
            if (isInputValid()) {
                event.eventName = binding.editTextEventName.text.toString().trim()
                event.eventLocation = binding.editTextEventLocation.text.toString().trim()
                event.eventType = binding.spinnerEventType.text.toString().trim()
                event.eventDescription = binding.editTextEventDescription.text.toString().trim()

                dataViewModel.addEvent(event)

                Toast.makeText(requireContext(), "Event shared! 🎉", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_add_event_to_timeline)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Checks if all input fields are valid.
     *
     * This method validates the user input by checking if all required fields (event name, location,
     * date range, type, and description) are filled out.
     *
     * @return true if all fields are filled, false otherwise.
     */
    private fun isInputValid(): Boolean {
        return binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDateRange.text.toString().isNotEmpty() &&
                binding.spinnerEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }
}

