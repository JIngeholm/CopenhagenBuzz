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
import android.os.Build
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
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.storage
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentAccountBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.EventLocation
import java.util.Locale

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val dataViewModel: DataViewModel by activityViewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private val user get() = dataViewModel.auth.currentUser

    private val binding get() = requireNotNull(_binding) {
        "Cannot access binding because it is null. Is the view visible?"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        setupImagePicker()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        setupClickListeners()

        // Add auth state listener
        dataViewModel.auth.addAuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let { updatedUser ->
                // Update UI with fresh user data
                binding.usernameInput.setText(updatedUser.displayName ?: "")

                Picasso.get()
                    .load(updatedUser.photoUrl)
                    .placeholder(R.drawable.guest_24)
                    .error(R.drawable.guest_24)
                    .into(binding.profileImage)
            }
        }
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.profileImage.setImageURI(uri)
                }
            }
        }
    }

    private fun initializeViews() {
        // Load current profile data
        user?.let {
            binding.usernameInput.setText(it.displayName ?: "")

            Picasso.get()
                .load(it.photoUrl)
                .placeholder(R.drawable.guest_24)
                .error(R.drawable.guest_24)
                .into(binding.profileImage)
        }
    }

    private fun setupClickListeners() {
        binding.changeImageButton.setOnClickListener {
            openImagePicker()
        }

        binding.profileImage.setOnClickListener{
            openImagePicker()
        }

        binding.saveButton.setOnClickListener {
            updateUserProfile()
            Toast.makeText(requireContext(), "Profile saved! 🎉", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_account_to_timeline)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun updateUserProfile() {
        val newName = binding.usernameInput.text.toString().trim()
        if (newName.isEmpty()) {
            binding.usernameInputLayout.error = "Please enter a username"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.saveButton.isEnabled = false

        if (selectedImageUri != null) {
            // Upload new image first
            uploadImageAndUpdateProfile(newName)
        } else {
            // Just update the name
            updateFirebaseProfile(newName, user?.photoUrl?.toString())
        }
    }

    private fun uploadImageAndUpdateProfile(newName: String) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("profile_images/${user?.uid}.jpg")

        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    // Get download URL after upload
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateFirebaseProfile(newName, downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.saveButton.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun updateFirebaseProfile(name: String, photoUrl: String?) {
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .apply {
                if (photoUrl != null) {
                    setPhotoUri(Uri.parse(photoUrl))
                }
            }
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                binding.saveButton.isEnabled = true

                if (task.isSuccessful) {
                    // Force refresh of user data
                    user?.reload()?.addOnCompleteListener { reloadTask ->
                        if (reloadTask.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showUpdateSuccessWithPossibleDelay()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update profile: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showUpdateSuccessWithPossibleDelay() {
        Toast.makeText(
            requireContext(),
            "Profile updated! Changes may take a moment to appear",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the auth state listener when fragment is destroyed
        dataViewModel.auth.removeAuthStateListener { /* listener will be automatically removed */ }
        _binding = null
    }
}

