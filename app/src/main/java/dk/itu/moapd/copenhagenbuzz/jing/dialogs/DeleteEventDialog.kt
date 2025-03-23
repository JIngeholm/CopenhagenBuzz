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
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogDeleteEventBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

/**
 * A dialog fragment for confirming the deletion of an event.
 *
 * @property event The event to be deleted.
 */
class DeleteEventDialog(val event: Event) : DialogFragment() {

    // View binding for the delete event dialog
    private var _binding: DialogDeleteEventBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel for handling event data
    private val dataViewModel: DataViewModel by activityViewModels()

    /**
     * Creates and returns a dialog for confirming event deletion.
     *
     * @param savedInstanceState The saved instance state bundle.
     * @return A configured [Dialog] instance.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDeleteEventBinding.inflate(layoutInflater)

        // Cancel button dismisses the dialog
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        // Delete button removes the event via ViewModel and dismisses the dialog
        binding.deleteButton.setOnClickListener {
            dataViewModel.deleteEvent(event)
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    /**
     * Cleans up view binding when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
