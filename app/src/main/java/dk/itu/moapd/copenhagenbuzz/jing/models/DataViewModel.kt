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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
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
     * Deletes an event from Firebase using its reference, ensuring it is also removed from all users' favorites.
     *
     * @param eventReference The Firebase DatabaseReference pointing to the event to be deleted.
     */
    fun deleteEvent(eventReference: DatabaseReference) {
        // Get the event ID from the reference path
        val eventId = eventReference.key ?: run {
            Log.e("DeleteEvent", "Event reference has no key")
            return
        }

        val databaseRef = database.reference

        // First handle the favorites cleanup
        eventReference.child("favoritedBy")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userId = userSnapshot.key
                            userId?.let {
                                databaseRef.child("favorites").child(userId).child(eventId).removeValue()
                                    .addOnSuccessListener {
                                        Log.d("DeleteEvent", "Event removed from favorites of user: $userId")
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e("DeleteEvent", "Failed to remove from favorites for user: $userId", error)
                                    }
                            }
                        }
                    }

                    // Then delete the event itself
                    eventReference.removeValue()
                        .addOnSuccessListener {
                            Log.d("DeleteEvent", "Event successfully deleted")
                        }
                        .addOnFailureListener { error ->
                            Log.e("DeleteEvent", "Failed to delete event", error)
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
    fun updateEvent(editedEvent: Event) {
        val databaseRef = Firebase.database(DATABASE_URL).reference

        databaseRef.child("events").child(editedEvent.eventID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get existing data
                        val favoritedBy = dataSnapshot.child("favoritedBy").value as? MutableMap<String, Boolean>
                            ?: mutableMapOf()

                        editedEvent.favoritedBy = favoritedBy

                        // Perform update
                        databaseRef.child("events").child(editedEvent.eventID).setValue(editedEvent)
                            .addOnSuccessListener {
                                Log.d("EditEvent", "Event updated in events table")
                                // Update favorites
                                favoritedBy.forEach { (userId, _) ->
                                    databaseRef.child("favorites").child(userId)
                                        .child(editedEvent.eventID).setValue(editedEvent)
                                        .addOnSuccessListener {
                                            Log.d("EditEvent", "Event updated in favorites for user: $userId")
                                        }
                                        .addOnFailureListener { error ->
                                            Log.e("EditEvent", "Failed to update favorites for user: $userId", error)
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

    fun updateInvite(event: Event, unInvitedUsers: MutableList<String>) {
        val invitesRef = Firebase.database(DATABASE_URL).reference.child("invites")

        // Convert Event to a simplified map for invites
        val inviteData = mapOf(
            "eventId" to event.eventID,
            "eventName" to event.eventName,
            "eventStartDate" to event.eventStartDate,
            "eventLocation" to event.eventLocation,
            "eventPhoto" to event.eventPhoto,
            "eventType" to event.eventType
        )

        // Handle sending invites to new users
        event.invitedUsers.forEach { (userId, _) ->
            invitesRef.child(userId).child(event.eventID)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            // Store invite data
                            invitesRef.child(userId).child(event.eventID)
                                .setValue(inviteData)
                                .addOnSuccessListener {
                                    Log.d("Firebase", "Invite sent to $userId")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firebase", "Failed to invite $userId", e)
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Check invite failed", error.toException())
                    }
                })
        }

        // Handle removing invites from uninvited users
        unInvitedUsers.forEach { userId ->
            invitesRef.child(userId).child(event.eventID).removeValue()
                .addOnSuccessListener {
                    Log.d("Firebase", "Invite removed from $userId")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to remove invite", e)
                }
        }
    }
}
