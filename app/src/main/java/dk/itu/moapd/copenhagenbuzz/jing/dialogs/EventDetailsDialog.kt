package dk.itu.moapd.copenhagenbuzz.jing.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.DialogEventDetailsBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event

class EventDetailsDialog : DialogFragment() {
    private var _binding: DialogEventDetailsBinding? = null
    private val binding get() = _binding!!

    private val dataViewModel: DataViewModel by activityViewModels()
    private var eventId: String = ""

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        fun newInstance(eventId: String): EventDetailsDialog {
            val fragment = EventDetailsDialog()
            val args = Bundle()
            args.putString(ARG_EVENT_ID, eventId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getString(ARG_EVENT_ID) ?: ""
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEventDetailsBinding.inflate(layoutInflater)

        val query = database.reference
            .child("events")
            .orderByChild("eventStartDate")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events: List<Event> = snapshot.children.mapNotNull { it.getValue(Event::class.java) }

                // Find the event
                val event = events.find { it.eventID == eventId }

                // Populate UI with event details
                event?.let {
                    binding.textViewEventName.text = it.eventName
                    binding.textViewEventType.text = it.eventType
                    binding.textViewEventLocation.text = it.eventLocation.address
                    binding.textViewEventDate.text = it.eventStartDate
                    binding.textViewEventDescription.text = it.eventDescription

                    // Load image
                    Picasso.get()
                        .load(it.eventPhoto)
                        .placeholder(R.drawable.baseline_refresh_24)
                        .error(R.drawable.baseline_image_not_supported_24)
                        .into(binding.imageViewEvent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch events", error.toException())
            }
        })

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}