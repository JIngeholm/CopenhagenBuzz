package dk.itu.moapd.copenhagenbuzz.jing.adapters

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FavoriteRowItemBinding

class FavoriteAdapter(options: FirebaseRecyclerOptions<Event>) : FirebaseRecyclerAdapter<Event, FavoriteAdapter.ViewHolder>(options) {

    inner class ViewHolder(private val binding: FavoriteRowItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.eventName.text = event.eventName
            binding.eventType.text = event.eventType
            binding.circleText.text = event.eventType.firstOrNull()?.toString() ?: ""

            // Load the event photo using Picasso with a 90-degree rotation
            Picasso.get()
                .load(event.eventPhoto)
                .placeholder(R.drawable.baseline_image_not_supported_24)
                .error(R.drawable.event_photo_placeholder)
                .transform(object : Transformation {
                    override fun transform(source: Bitmap): Bitmap {
                        val matrix = Matrix().apply { postRotate(90f) }
                        val rotatedBitmap = Bitmap.createBitmap(
                            source, 0, 0, source.width, source.height, matrix, true
                        )
                        source.recycle() // Prevent memory leaks
                        return rotatedBitmap
                    }

                    override fun key(): String = "rotate90"
                })
                .into(binding.eventPhoto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FavoriteRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Event) {
        Log.d("TimeLineAdapter", "Populating item at position: $position")
        holder.bind(model)
    }
}
