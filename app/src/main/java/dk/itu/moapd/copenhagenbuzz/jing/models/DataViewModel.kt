import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
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
        auth.currentUser?.let { _ ->
            // Get a reference to Firebase Realtime Database
            val databaseReference = FirebaseDatabase.getInstance().reference

            Log.d("DataViewModel", "db reference = $databaseReference")

            // Push a new event to the database under the user's UID
            val eventReference = databaseReference.child("events")
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

    fun toggleFavorite(event: Event, liked: Boolean) : Boolean {
        auth.currentUser?.let { user ->
            // Get a reference to Firebase Realtime Database
            val databaseReference = FirebaseDatabase.getInstance().reference

            // Reference to the user's favorites
            val favoritesRef = databaseReference.child("favorites").child(user.uid)

            if (liked) {
                // If the event is already liked, remove it from favorites
                favoritesRef.child(event.eventID).removeValue()
                    .addOnSuccessListener {
                        Log.d("Firebase", "Event removed from favorites")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error removing event from favorites", exception)
                    }
            } else {
                // If the event is not liked, add it to favorites
                favoritesRef.child(event.eventID).setValue(event)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Event added to favorites")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error adding event to favorites", exception)
                    }
            }
        } ?: run {
            Log.e("Firebase", "User is not authenticated")
        }

        return !liked
    }

    fun deleteEvent(event: Event){
        // Get a reference to the Firebase database
        val databaseRef = Firebase.database(DATABASE_URL).reference

        // Step 1: Delete the event from the 'events' table
        databaseRef.child("events").child(event.eventID).removeValue()
            .addOnSuccessListener {
                Log.d("DeleteEvent", "Event deleted from events table")

                // Step 2: Delete the event from the 'favorites' table for all users
                databaseRef.child("favorites").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Iterate through all users in the 'favorites' table
                        for (userSnapshot in dataSnapshot.children) {
                            val userId = userSnapshot.key // Get the user ID
                            val userFavoritesRef = databaseRef.child("favorites").child(userId ?: "")

                            // Check if the event exists in the user's favorites
                            userFavoritesRef.child(event.eventID).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(favoriteSnapshot: DataSnapshot) {
                                    if (favoriteSnapshot.exists()) {
                                        // Delete the event from the user's favorites
                                        userFavoritesRef.child(event.eventID).removeValue()
                                            .addOnSuccessListener {
                                                Log.d("DeleteEvent", "Event deleted from favorites for user: $userId")
                                            }
                                            .addOnFailureListener { error ->
                                                Log.e("DeleteEvent", "Failed to delete event from favorites for user: $userId", error)
                                            }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("DeleteEvent", "Failed to check favorites for user: $userId", error.toException())
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DeleteEvent", "Failed to fetch favorites table", error.toException())
                    }
                })
            }
            .addOnFailureListener { error ->
                Log.e("DeleteEvent", "Failed to delete event from events table", error)
            }
    }

    fun editEvent(editedEvent: Event) {
        // Get a reference to the Firebase database
        val databaseRef = Firebase.database(DATABASE_URL).reference

        // Step 1: Update the event in the 'events' table
        databaseRef.child("events").child(editedEvent.eventID).setValue(editedEvent)
            .addOnSuccessListener {
                Log.d("EditEvent", "Event updated in events table")

                // Step 2: Update the event in the 'favorites' table for all users
                databaseRef.child("favorites").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Iterate through all users in the 'favorites' table
                        for (userSnapshot in dataSnapshot.children) {
                            val userId = userSnapshot.key // Get the user ID
                            val userFavoritesRef = databaseRef.child("favorites").child(userId ?: "")

                            // Check if the event exists in the user's favorites
                            userFavoritesRef.child(editedEvent.eventID).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(favoriteSnapshot: DataSnapshot) {
                                    if (favoriteSnapshot.exists()) {
                                        // Update the event in the user's favorites
                                        userFavoritesRef.child(editedEvent.eventID).setValue(editedEvent)
                                            .addOnSuccessListener {
                                                Log.d("EditEvent", "Event updated in favorites for user: $userId")
                                            }
                                            .addOnFailureListener { error ->
                                                Log.e("EditEvent", "Failed to update event in favorites for user: $userId", error)
                                            }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("EditEvent", "Failed to check favorites for user: $userId", error.toException())
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("EditEvent", "Failed to fetch favorites table", error.toException())
                    }
                })
            }
            .addOnFailureListener { error ->
                Log.e("EditEvent", "Failed to update event in events table", error)
            }
    }



}
