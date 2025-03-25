package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.jing.databinding.InboxRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event

class InboxAdapter(
    private val events: List<Event>,
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    private var attendClickListener: ((Event) -> Unit)? = null
    private var maybeClickListener: ((Event) -> Unit)? = null
    private var declineClickListener: ((Event) -> Unit)? = null

    inner class ViewHolder(private val binding: InboxRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {

            // Get reference to the user in Firebase
            val userRef = Firebase.database(DATABASE_URL).reference
                .child("users")
                .child(event.userId)

            // Fetch the user data
            userRef.get().addOnSuccessListener { snapshot ->
                val username =
                    snapshot.child("username").getValue(String::class.java) ?: "Unknown user"
                    binding.eventInvitationText.text = "$username invited you to an event!"
            }

            binding.eventName.text = event.eventName
            binding.eventType.text = event.eventType

            binding.attend.setOnClickListener {
                attendClickListener?.invoke(event)
            }
            binding.maybe.setOnClickListener {
                maybeClickListener?.invoke(event)
            }
            binding.decline.setOnClickListener {
                declineClickListener?.invoke(event)
            }
        }
    }

    fun setOnAttendClickListener(listener: (Event) -> Unit) {
        attendClickListener = listener
    }

    fun setOnMaybeClickListener(listener: (Event) -> Unit) {
        maybeClickListener = listener
    }

    fun setOnDeclineClickListener(listener: (Event) -> Unit) {
        declineClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InboxRowItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size
}