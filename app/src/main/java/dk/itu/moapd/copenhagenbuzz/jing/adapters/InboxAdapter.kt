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

package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.databinding.InboxRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event

class InboxAdapter(
    private val events: List<Event>
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    private var attendClickListener: ((Event) -> Unit)? = null
    private var maybeClickListener: ((Event) -> Unit)? = null
    private var declineClickListener: ((Event) -> Unit)? = null

    inner class ViewHolder(
        private val binding: InboxRowItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            // Get reference to the user in Firebase
            val userRef = database.reference
                .child("users")
                .child(event.userId)

            // Fetch the user data
            userRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username")
                    .getValue(String::class.java) ?: "Unknown user"
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
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}