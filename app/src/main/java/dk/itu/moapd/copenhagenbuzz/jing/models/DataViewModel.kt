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
            val faker = Faker()
            val newEvents = ArrayList<Event>()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startDate = faker.date().future(10, TimeUnit.DAYS)
            val endDate = faker.date().future(15, TimeUnit.DAYS)

            val date = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"

            val types = listOf("Music", "Party", "Toilet", "Sport", "Art")

            for (i in 1..10) {
                val random = Random.nextInt(types.size)
                val randomEventType = types[random]
                val event = Event(
                    eventName = faker.lorem().word(),
                    eventType = randomEventType,
                    eventPhoto = faker.internet().image(),
                    eventLocation = faker.address().city(),
                    eventDate = date,
                    eventDescription = faker.lorem().sentence()
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
}


