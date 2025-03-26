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

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.UserRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser

class InviteAdapter(
    options: FirebaseRecyclerOptions<buzzUser>,
    dataViewModel: DataViewModel,
    private val event: Event,
    private val onUserChecked: (buzzUser, Boolean) -> Unit
) : FirebaseRecyclerAdapter<buzzUser, InviteAdapter.ViewHolder>(options) {

    private val currentUserUid = dataViewModel.auth.currentUser?.uid

    inner class ViewHolder(
        private val binding: UserRowItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: buzzUser, isChecked: Boolean) {
            binding.Name.text = user.username

            Picasso.get()
                .load(user.profilePicture)
                .placeholder(R.drawable.guest_24)
                .error(R.drawable.guest_24)
                .into(binding.profilePicture)

            binding.checkbox.isChecked = isChecked

            binding.checkbox.setOnCheckedChangeListener { _, isCheckedNew ->
                onUserChecked(user, isCheckedNew)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserRowItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: buzzUser) {
        val isChecked = event.invitedUsers.contains(model.uid)
        Log.d("InviteAdapter", "Populating item at position: $position")
        holder.bind(model, isChecked)
    }

    override fun getItemCount(): Int {
        return super.getItemCount() - countCurrentUser()
    }

    override fun getItem(position: Int): buzzUser {
        var adjustedPosition = position
        for (i in 0..position) {
            if (super.getItem(i).uid == currentUserUid) {
                adjustedPosition++
            }
        }
        return super.getItem(adjustedPosition)
    }

    private fun countCurrentUser(): Int {
        var count = 0
        for (i in 0 until super.getItemCount()) {
            if (super.getItem(i).uid == currentUserUid) {
                count++
            }
        }
        return count
    }
}