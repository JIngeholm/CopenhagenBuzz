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
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.storage
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.EventLocation
import java.io.File
import java.util.Locale

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
    private val event: Event = Event("", EventLocation(), "", "", "", "", "", "")
    private val dataViewModel: DataViewModel by activityViewModels()
    private val geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }
    private var currentLatLng: LatLng? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var photoUri: Uri? = null


    companion object {
        private const val IMAGE_REQUEST_CODE = 100
    }

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                imageUri?.let {
                    binding.imageViewEventPicture.setImageURI(it)
                    event.eventPhoto = it.toString()
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && photoUri != null) {
                binding.imageViewEventPicture.setImageURI(photoUri)
                event.eventPhoto = photoUri.toString()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        event.userId = dataViewModel.auth.currentUser?.uid ?: "unknown user"
        initializeViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeViews() {
        binding.editTextEventDateRange.setOnClickListener {
            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event start date")
                .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { startDateMillis ->
                val startDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(
                    Date(startDateMillis)
                )
                event.eventStartDate = startDate

                val endDatePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select event end date")
                    .setSelection(startDateMillis)
                    .build()

                endDatePicker.show(parentFragmentManager, "END_DATE_PICKER")

                endDatePicker.addOnPositiveButtonClickListener { endDateMillis ->
                    val endDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(
                        Date(endDateMillis)
                    )
                    event.eventEndDate = endDate
                    binding.editTextEventDateRange.setText(
                        getString(R.string.event_date_range_format, startDate, endDate)
                    )
                }
            }
        }

        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.captureImageButton.setOnClickListener {
            launchCamera()
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
            setOnClickListener { showDropDown() }
        }

        binding.addEventButton.setOnClickListener {
            if (isInputValid()) {
                val locationName = binding.editTextEventLocation.text.toString().trim()
                geocodeLocation(locationName) { success ->
                    if (success) {
                        saveEvent()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Could not find location. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun geocodeLocation(locationName: String, callback: (Boolean) -> Unit) {
        try {
            if (Geocoder.isPresent()) {
                val addresses = geocoder.getFromLocationName(locationName, 1)
                if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]
                    currentLatLng = LatLng(address.latitude, address.longitude)
                    event.eventLocation = EventLocation(
                        latitude = address.latitude,
                        longitude = address.longitude,
                        address = address.getAddressLine(0) ?: locationName
                    )
                    callback(true)
                } else {
                    callback(false)
                }
            } else {
                callback(false)
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Error: ${e.message}")
            callback(false)
        }
    }

    private fun saveEvent() {
        event.apply {
            eventName = binding.editTextEventName.text.toString().trim()
            eventType = binding.spinnerEventType.text.toString().trim()
            eventDescription = binding.editTextEventDescription.text.toString().trim()
        }

        // Check if we have an image to upload
        if (event.eventPhoto.startsWith("content://") || event.eventPhoto.startsWith("file://")) {
            // This is a local URI - we need to upload it
            uploadImageAndSaveEvent(Uri.parse(event.eventPhoto))
        } else if (event.eventPhoto.isEmpty()) {
            // No image - save event directly
            dataViewModel.addEvent(event)
            showSuccessAndNavigate()
        }
    }

    private fun uploadImageAndSaveEvent(imageUri: Uri) {

        val imageRef = storage.reference.child("event_images/${System.currentTimeMillis()}.jpg")

        binding.addEventButton.isEnabled = false

        // Upload the file
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Update event with download URL
                    event.eventPhoto = uri.toString()
                    // Save event to database
                    dataViewModel.addEvent(event)
                    showSuccessAndNavigate()
                }
            }
            .addOnFailureListener { exception ->
                binding.addEventButton.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Failed to upload image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showSuccessAndNavigate() {
        binding.addEventButton.isEnabled = true
        Toast.makeText(requireContext(), "Event shared! 🎉", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_add_event_to_timeline)
    }

    private fun isInputValid(): Boolean {
        return binding.editTextEventName.text.toString().isNotEmpty() &&
                binding.editTextEventLocation.text.toString().isNotEmpty() &&
                binding.editTextEventDateRange.text.toString().isNotEmpty() &&
                binding.spinnerEventType.text.toString().isNotEmpty() &&
                binding.editTextEventDescription.text.toString().isNotEmpty()
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "dk.itu.moapd.copenhagenbuzz.jing.fileprovider",
                photoFile
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
            cameraLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().cacheDir
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            Log.e("CameraIntent", "Error creating file: ${e.message}")
            null
        }
    }
}

