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

            // Push a new event to the database under the user's UID
            val eventReference = databaseReference.child("events")
                .child(user.uid)
                .push()

            eventReference.key?.let { uid ->
                // Insert the event object in the database
                eventReference.setValue(event)
                    .addOnSuccessListener {
                        // You can handle success here if needed
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure (e.g., log or show a message)
                    }
            }
        }
    }

    /**
     * Function to toggle the favorite status of an event in the Firebase Realtime Database.
     */
    /*
    fun toggleFavorite(event: Event) {

        // Get a reference to Firebase Realtime Database
        val databaseReference = FirebaseDatabase.getInstance().reference

        auth.currentUser?.let { user ->
            // If the event is already liked, remove it from the favorites
            if (event.liked) {
                databaseReference.orderByChild("eventId").equalTo(event.eventId).get()
                    .addOnSuccessListener { snapshot ->
                        for (child in snapshot.children) {
                            child.ref.removeValue() // Remove the event from the "favorites" table
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure (e.g., log or show a message)
                        exception.printStackTrace()
                    }
            } else {
                // If the event is not liked, add it to the favorites
                val eventReference = favoritesRef.push()

                eventReference.key?.let { uid ->
                    // Insert the event object in the database
                    eventReference.setValue(event)
                        .addOnSuccessListener {
                            // Optionally handle success here
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure (e.g., log or show a message)
                            exception.printStackTrace()
                        }
                }
            }

            // Toggle the 'liked' state of the event
            event.liked = !event.liked
        }
    }
     */


}
