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

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.javafaker.Faker
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * ViewModel responsible for managing event data and user authentication state.
 *
 * The [DataViewModel] handles fetching, storing, and providing event data using LiveData.
 * It also maintains the login status of the user and provides methods to generate mock event data.
 */
class DataViewModel : ViewModel() {

    /**
     * Application context, used when necessary.
     * We need to do this differently to remove warnings.
     */
    private var context: Context? = null

    /**
     * Tracks whether mock events have been generated to prevent duplication.
     */
    private var areMockEventsGenerated = false

    /**
     * Sets the application context.
     *
     * @param context The application context.
     */
    fun setContext(context: Context) {
        this.context = context
    }

    /**
     * LiveData representing the user's login status.
     */
    val isLoggedIn = MutableLiveData<Boolean>()

    /**
     * LiveData holding the list of events.
     */
    private val _events = MutableLiveData<List<Event>?>(emptyList())

    /**
     * Public accessor for events LiveData.
     */
    val events: MutableLiveData<List<Event>?> get() = _events

    /**
     * Fetches a list of events, generating mock data if not already generated.
     *
     * This method ensures that mock events are created only once and appended
     * to the current list of events.
     *
     * @return A list of events in an [ArrayList].
     */
    fun fetchEvents(): ArrayList<Event> {
        if (!areMockEventsGenerated) {
            val faker = Faker(Locale.ENGLISH)
            val newEvents = ArrayList<Event>()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startDate = faker.date().future(10, TimeUnit.DAYS)
            val endDate = faker.date().future(15, TimeUnit.DAYS)

            val date = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"

            val types = listOf("Music", "Party", "Sport", "Art", "School", "Dinner", "Lunch", "Duel")

            for (i in 1..10) {
                val random = Random.nextInt(types.size)
                val randomEventType = types[random]
                val event = Event(
                    eventName = faker.commerce().productName(),
                    eventType = randomEventType,
                    eventPhoto = "https://picsum.photos/400/300?random=${Random.nextInt(1000)}",
                    eventLocation = faker.address().city(),
                    eventDate = date,
                    eventDescription = faker.shakespeare().hamletQuote(),
                    liked = false
                )
                newEvents.add(event)
            }

            // Append new mock events to the existing list
            val currentEvents = _events.value ?: emptyList()
            _events.value = currentEvents + newEvents
            areMockEventsGenerated = true
        }
        return _events.value as ArrayList<Event>
    }

    /**
     * Adds a new event to the list and updates the LiveData.
     *
     * The new event is appended to the list, and the order is reversed to maintain a specific order.
     *
     * @param event The event to be added.
     */
    fun addEvent(event: Event) {
        val currentEvents = _events.value
        var updatedEvents = currentEvents?.plus(event)
        if (updatedEvents != null) {
            updatedEvents = updatedEvents.reversed()
        }
        _events.value = updatedEvents // Update the LiveData
    }

    /**
     * LiveData holding the list of favorite events.
     */
    private val _favorites = MutableLiveData<List<Event>?>()

    /**
     * Public accessor for favorites LiveData.
     */
    val favorites: MutableLiveData<List<Event>> get() = _favorites

    /**
     * Generates a random sample of favorite events from the available events.
     * This method takes a list of events and randomly selects 25 to add to the favorites list.
     *
     * @param events The list of events to choose from.
     * @return A list of 25 random events.
     */
    private fun generateRandomFavorites(events: List<Event>): List<Event> {
        val shuffledIndices = (events.indices).shuffled().take(25).sorted()
        return shuffledIndices.mapNotNull { index -> events.getOrNull(index) }
    }

    /**
     * Updates the favorite events list with a random sample of events.
     * This will trigger observers of the 'favorites' LiveData.
     */
    fun updateFavorites() {
        val currentEvents = _events.value ?: emptyList()
        val favoriteEvents = generateRandomFavorites(currentEvents)
        _favorites.value = favoriteEvents // Update the LiveData with the favorite events
    }

    /**
     * Adds an event to the favorites list if it's liked.
     * Updates the 'favorites' LiveData after adding the event.
     *
     * @param event The event to be liked and added to the favorites.
     */
    fun toggleFavorite(event: Event) {
        // Toggle the 'liked' status of the event
        val updatedEvent = event.copy(liked = !event.liked)

        // Update the events list with the updated event (toggle liked status)
        val currentEvents = _events.value?.map {
            if (it == event) updatedEvent else it
        }
        _events.value = currentEvents // Update LiveData with the new event list

        // Add to the favorites list if liked
        if (updatedEvent.liked) {
            val currentFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
            currentFavorites.add(updatedEvent) // Add the event to favorites
            _favorites.value = currentFavorites // Update LiveData for favorites
        } else {
            // Remove from favorites if no longer liked
            val currentFavorites = _favorites.value?.toMutableList()
            currentFavorites?.remove(updatedEvent)
            _favorites.value = currentFavorites // Update LiveData for favorites
        }
    }
}


