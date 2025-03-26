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

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogDeleteEventBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

class DeleteEventDialog : DialogFragment() {

    // View binding properties
    private var _binding: DialogDeleteEventBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val dataViewModel: DataViewModel by activityViewModels()

    // Dialog arguments
    private var eventId: String = ""

    companion object {
        /**
         * Creates a new instance of DeleteEventDialog with the specified event ID.
         *
         * @param eventId The ID of the event to be deleted
         * @return A new instance of DeleteEventDialog
         */
        fun newInstance(eventId: String): DeleteEventDialog {
            return DeleteEventDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_EVENT_ID, eventId)
                }
            }
        }

        private const val ARG_EVENT_ID = "event_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getString(ARG_EVENT_ID).orEmpty()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Initialize view binding
        _binding = DialogDeleteEventBinding.inflate(layoutInflater)

        // Get Firebase reference for the event
        val eventRef = database.reference
            .child("events")
            .child(eventId)

        setupButtonListeners(eventRef)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupButtonListeners(eventRef: DatabaseReference) {
        binding.apply {
            cancelButton.setOnClickListener { dismiss() }

            deleteButton.setOnClickListener {
                dataViewModel.deleteEvent(eventRef)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}