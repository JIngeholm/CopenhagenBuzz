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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * ViewModel that handles the business logic for managing events and favorites.
 *
 * This ViewModel is responsible for fetching, adding, and managing events, as well as toggling favorites.
 */
class DataViewModel : ViewModel() {

    /**
     * Tracks whether mock events have been generated to prevent duplication.
     */
    private var areMockEventsGenerated = false

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
    val events: LiveData<List<Event>?> get() = _events

    /**
     * Fetches a list of events asynchronously using coroutines.
     * This simulates fetching events from a remote source or database.
     */
    fun fetchEvents() {
        viewModelScope.launch {
            val events = withContext(Dispatchers.IO) {
                generateMockEvents() // Generate mock data in the background
            }
            _events.value = events
        }
    }

    /**
     * Generates mock events if not already generated.
     * Uses Faker to generate random event data.
     *
     * @return A list of mock events.
     */
    private fun generateMockEvents(): List<Event> {
        if (!areMockEventsGenerated) {
            val faker = Faker(Locale.ENGLISH)
            val newEvents = ArrayList<Event>()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = faker.date().future(10, TimeUnit.DAYS)
            val endDate = faker.date().future(15, TimeUnit.DAYS)
            val date = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"

            val types = listOf("Music", "Party", "Sport", "Art", "School", "Dinner", "Lunch", "Duel")

            for (i in 1..50) {
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

            areMockEventsGenerated = true
            return newEvents
        }
        return _events.value ?: emptyList()
    }

    /**
     * Adds a new event to the list and updates the LiveData.
     * The new event is added to the beginning of the list.
     *
     * @param event The event to add to the list.
     */
    fun addEvent(event: Event) {
        val currentEvents = _events.value
        val updatedEvents = currentEvents?.plus(event)?.reversed()
        _events.value = updatedEvents
    }

    /**
     * Loads the events by fetching them asynchronously.
     */
    fun loadEvents() {
        fetchEvents()
    }

    /**
     * LiveData holding the list of favorite events.
     */
    private val _favorites = MutableLiveData<List<Event>?>(emptyList())

    /**
     * Public accessor for favorites LiveData.
     */
    val favorites: LiveData<List<Event>?> get() = _favorites

    /**
     * Toggles the liked status of an event and updates the favorites list.
     * If the event is liked, it is added to the favorites list; if it is unliked, it is removed.
     *
     * @param event The event whose liked status is toggled.
     */
    fun toggleFavorite(event: Event) {
        val updatedEvent = event.copy(liked = !event.liked)

        // Update the events list
        val currentEvents = _events.value?.map {
            if (it == event) updatedEvent else it
        }
        _events.value = currentEvents

        // Update the favorites list
        val currentFavorites = _favorites.value ?: emptyList()
        val updatedFavorites = if (updatedEvent.liked) {
            currentFavorites + updatedEvent // Add the event to favorites
        } else {
            currentFavorites - event // Remove the event from favorites
        }
        _favorites.value = updatedFavorites
    }

    /**
     * Fetches the list of favorite events.
     *
     * @return A list of favorite events.
     */
    fun fetchFavorites(): List<Event>? {
        // Replace this with actual data fetching logic if needed
        return _favorites.value
    }
}



