package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogDeleteEventBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

class DeleteEventDialog(val event: Event) : DialogFragment() {

    private var _binding: DialogDeleteEventBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDeleteEventBinding.inflate(layoutInflater)

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.deleteButton.setOnClickListener {
            dataViewModel.deleteEvent(event)
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
