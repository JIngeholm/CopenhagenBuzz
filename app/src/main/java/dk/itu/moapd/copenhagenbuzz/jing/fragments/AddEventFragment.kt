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

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.javafaker.Faker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentAddEventBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import java.text.SimpleDateFormat
import java.util.Date

class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null

    val faker = Faker()

    private val event: Event = Event("", "", "", "", "", faker.internet().image())

    private lateinit var dataViewModel: DataViewModel  // Declare the DataViewModel

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

        // Get the DataViewModel from the ViewModelProvider
        dataViewModel = ViewModelProvider(requireActivity()).get(DataViewModel::class.java)

        initializeViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Initializes the UI components and sets up event listeners for user interactions.
     */
    private fun initializeViews() {

        binding.editTextEventDateRange.setOnClickListener {
            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event start date")
                .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { startDateMillis ->
                val startDate = SimpleDateFormat("MM-dd-yyyy", java.util.Locale.getDefault()).format(
                    Date(startDateMillis)
                )

                val endDatePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event end date")
                    .setSelection(startDateMillis)
                    .build()

                endDatePicker.show(parentFragmentManager, "END_DATE_PICKER")

                endDatePicker.addOnPositiveButtonClickListener { endDateMillis ->
                    val endDate = SimpleDateFormat("MM-dd-yyyy", java.util.Locale.getDefault()).format(
                        Date(endDateMillis)
                    )
                    val formattedDateRange = getString(dk.itu.moapd.copenhagenbuzz.jing.R.string.event_date_range_format, startDate, endDate)
                    binding.editTextEventDateRange.setText(formattedDateRange)
                }
            }
        }

        val eventTypes = resources.getStringArray(dk.itu.moapd.copenhagenbuzz.jing.R.array.event_types)
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

                Toast.makeText(requireContext(), "Event shared! 🎉", Toast.LENGTH_SHORT).show()

                dataViewModel.addEvent(event)

                findNavController().navigate(R.id.action_add_event_to_timeline)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
