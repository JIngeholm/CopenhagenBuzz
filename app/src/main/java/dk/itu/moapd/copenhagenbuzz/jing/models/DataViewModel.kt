import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.copenhagenbuzz.jing.data.Event

/**
 * ViewModel that handles the business logic for managing events and favorites.
 *
 * This ViewModel is responsible for fetching, adding, and managing events, as well as toggling favorites.
 */
class DataViewModel : ViewModel() {

    /**
     * LiveData representing the user's login status.
     */
    val isLoggedIn = MutableLiveData<Boolean>()

    // Initialize FirebaseAuth
    lateinit var auth: FirebaseAuth

    /**
     * Function to insert an event into the Firebase Realtime Database.
     */
    fun addEvent(event: Event) {
        auth.currentUser?.let { user ->
            // Get a reference to Firebase Realtime Database
            val databaseReference = FirebaseDatabase.getInstance().reference

            Log.d("DataViewModel", "db reference = $databaseReference")

            // Push a new event to the database under the user's UID
            val eventReference = databaseReference.child("events")
                .child(user.uid)
                .push()

            event.eventID = eventReference.key.toString()

            eventReference.key?.let { _ ->
                // Insert the event object in the database
                eventReference.setValue(event)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Event added successfully!")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error adding event", exception)
                    }
            }
        } ?: run {
            Log.e("Firebase", "User is not authenticated")
        }
    }

    /**
     * Function to toggle the favorite status of an event in the Firebase Realtime Database.
     */

    fun toggleFavorite(event: Event) {
        auth.currentUser?.let { user ->
            // Get a reference to Firebase Realtime Database
            val databaseReference = FirebaseDatabase.getInstance().reference

            // Reference to the user's favorites
            val favoritesRef = databaseReference.child("favorites").child(user.uid)

            if (event.liked) {
                // If the event is already liked, remove it from favorites
                favoritesRef.child(event.eventID).removeValue()
                    .addOnSuccessListener {
                        Log.d("Firebase", "Event removed from favorites")
                        event.liked = false // Update the local state
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error removing event from favorites", exception)
                    }
            } else {
                // If the event is not liked, add it to favorites
                favoritesRef.child(event.eventID).setValue(event)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Event added to favorites")
                        event.liked = true // Update the local state
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error adding event to favorites", exception)
                    }
            }
        } ?: run {
            Log.e("Firebase", "User is not authenticated")
        }
    }



}
