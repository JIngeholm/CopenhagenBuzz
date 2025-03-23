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

package dk.itu.moapd.copenhagenbuzz.jing.models

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
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event

/**
 * ViewModel responsible for handling event-related operations and user authentication state.
 */
class DataViewModel : ViewModel() {

    /**
     * LiveData representing the user's login status.
     */
    val isLoggedIn = MutableLiveData<Boolean>()

    /**
     * Firebase Authentication instance used to manage user authentication.
     */
    lateinit var auth: FirebaseAuth

    /**
     * Adds a new event to Firebase Realtime Database.
     *
     * @param event The [Event] object to be added.
     */
    fun addEvent(event: Event) {
        auth.currentUser?.let { _ ->
            val databaseReference = FirebaseDatabase.getInstance().reference
            Log.d("DataViewModel", "db reference = $databaseReference")

            val eventReference = databaseReference.child("events").push()
            event.eventID = eventReference.key.toString()

            eventReference.key?.let { _ ->
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
     * Toggles the favorite status of an event.
     *
     * @param event The [Event] whose favorite status is to be toggled.
     * @param liked The current like status of the event.
     * @return The updated favorite status.
     */
    fun toggleFavorite(event: Event, liked: Boolean): Boolean {
        auth.currentUser?.let { user ->
            val databaseRef = Firebase.database(DATABASE_URL).reference
            val favoritesRef = databaseRef.child("favorites").child(user.uid)
            val favoritedByRef = databaseRef.child("events").child(event.eventID).child("favoritedBy")

            if (liked) {
                favoritesRef.child(event.eventID).removeValue()
                    .addOnSuccessListener {
                        Log.d("Firebase", "Event removed from favorites")
                        favoritedByRef.child(user.uid).removeValue()
                            .addOnSuccessListener {
                                Log.d("Firebase", "User removed from favoritedBy")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firebase", "Error removing user from favoritedBy", exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error removing event from favorites", exception)
                    }
            } else {
                favoritesRef.child(event.eventID).setValue(event)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Event added to favorites")
                        favoritedByRef.child(user.uid).setValue(true)
                            .addOnSuccessListener {
                                Log.d("Firebase", "User added to favoritedBy")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firebase", "Error adding user to favoritedBy", exception)
                            }
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

    /**
     * Deletes an event from Firebase, ensuring it is also removed from all users' favorites.
     *
     * @param event The [Event] object to be deleted.
     */
    fun deleteEvent(event: Event) {
        val databaseRef = Firebase.database(DATABASE_URL).reference

        databaseRef.child("events").child(event.eventID).child("favoritedBy")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userId = userSnapshot.key
                            if (userId != null) {
                                databaseRef.child("favorites").child(userId).child(event.eventID).removeValue()
                                    .addOnSuccessListener {
                                        Log.d("DeleteEvent", "Event deleted from favorites for user: $userId")
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e("DeleteEvent", "Failed to delete event from favorites for user: $userId", error)
                                    }
                            }
                        }
                    }

                    databaseRef.child("events").child(event.eventID).removeValue()
                        .addOnSuccessListener {
                            Log.d("DeleteEvent", "Event deleted from events table")
                        }
                        .addOnFailureListener { error ->
                            Log.e("DeleteEvent", "Failed to delete event from events table", error)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DeleteEvent", "Failed to fetch favoritedBy list", error.toException())
                }
            })
    }

    /**
     * Edits an existing event in Firebase, ensuring the update reflects in all users' favorites.
     *
     * @param editedEvent The modified [Event] object.
     */
    fun editEvent(editedEvent: Event) {
        val databaseRef = Firebase.database(DATABASE_URL).reference

        databaseRef.child("events").child(editedEvent.eventID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val favoritedBy = dataSnapshot.child("favoritedBy").value as? MutableMap<String, Boolean>
                            ?: mutableMapOf()

                        editedEvent.favoritedBy = favoritedBy

                        databaseRef.child("events").child(editedEvent.eventID).setValue(editedEvent)
                            .addOnSuccessListener {
                                Log.d("EditEvent", "Event updated in events table")
                                for ((userId, _) in favoritedBy) {
                                    databaseRef.child("favorites").child(userId).child(editedEvent.eventID).setValue(editedEvent)
                                        .addOnSuccessListener {
                                            Log.d("EditEvent", "Event updated in favorites for user: $userId")
                                        }
                                        .addOnFailureListener { error ->
                                            Log.e("EditEvent", "Failed to update event in favorites for user: $userId", error)
                                        }
                                }
                            }
                            .addOnFailureListener { error ->
                                Log.e("EditEvent", "Failed to update event in events table", error)
                            }
                    } else {
                        Log.e("EditEvent", "Event not found in events table")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EditEvent", "Failed to fetch event data", error.toException())
                }
            })
    }
}
