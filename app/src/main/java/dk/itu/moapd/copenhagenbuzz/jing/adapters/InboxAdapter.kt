package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.R
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

            Picasso.get()
                .load(event.eventPhoto)
                .placeholder(R.drawable.baseline_image_not_supported_24)
                .error(R.drawable.event_photo_placeholder)
                .into(binding.eventPhoto)
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