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

class EditEventDialog(val event: Event) : DialogFragment() {

    private var _binding: DialogEditEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private val dataViewModel: DataViewModel by activityViewModels()

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

        intializeViews()

        handleModButtuns()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()

        // Set the dialog width and height
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT // Use full screen width
            val height = ViewGroup.LayoutParams.MATCH_PARENT // Use full screen width
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun handleModButtuns(){
        binding.cancelEditButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            if (isInputValid()) {
                dataViewModel.editEvent(getEditedEvent(event))
                Toast.makeText(requireContext(), "Event edited! 🎉", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }

            dismiss()
        }
    }




    fun getEditedEvent(event: Event) : Event {
        var editedEvent = Event("", "", "", "", "", "", "")

        editedEvent.userId = event.userId
        editedEvent.eventID = event.eventID
        editedEvent.eventPhoto = event.eventPhoto

        editedEvent.eventName = binding.editTextEventName.text.toString().trim()
        editedEvent.eventLocation = binding.editTextEventLocation.text.toString().trim()
        editedEvent.eventDate = binding.editTextEventDateRange.text.toString().trim()
        editedEvent.eventType = binding.spinnerEventType.text.toString().trim()
        editedEvent.eventDescription = binding.editTextEventDescription.text.toString().trim()

        return editedEvent
    }




    fun setInitialEventInfo(event: Event){
        binding.editTextEventName.setText(event.eventName)
        binding.editTextEventLocation.setText(event.eventLocation)
        binding.editTextEventDateRange.setText(event.eventDate)
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




    fun intializeViews(){
        // Set up date range picker
        binding.editTextEventDateRange.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event start date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { startDateMillis ->
                val startDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date(startDateMillis))

                val endDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event end date")
                    .setSelection(startDateMillis)
                    .build()

                endDatePicker.show(parentFragmentManager, "END_DATE_PICKER")

                endDatePicker.addOnPositiveButtonClickListener { endDateMillis ->
                    val endDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date(endDateMillis))
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
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, eventTypes)
        binding.spinnerEventType.setAdapter(adapter)
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
