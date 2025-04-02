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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.storage
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.activities.MainActivity
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentAccountBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

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

        binding.profileImage.setOnClickListener {
            openImagePicker()
        }

        binding.saveButton.setOnClickListener {
            updateUserProfile()
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
                binding.saveButton.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigate(R.id.action_account_to_timeline)
                    (activity as? MainActivity)?.setupDrawerHeader() // Force refresh

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update profile: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}