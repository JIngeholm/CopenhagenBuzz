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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import kotlinx.coroutines.launch
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * ViewModel class that fetches and manages event data.
 */
class DataViewModel : ViewModel() {

    private var context: Context? = null

    private var areMockEventsGenerated = false

    fun setContext(context: Context) {
        this.context = context
    }

    val isLoggedIn = MutableLiveData<Boolean>()
    
    // LiveData to hold the list of events
    private val _events = MutableLiveData<List<Event>?>(emptyList())
    val events: MutableLiveData<List<Event>?> get() = _events

    suspend fun fetchEvents(): ArrayList<Event> {
        if (!areMockEventsGenerated) {
            val faker = Faker()
            val newEvents = ArrayList<Event>()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startDate = faker.date().future(10, TimeUnit.DAYS)
            val endDate = faker.date().future(15, TimeUnit.DAYS)

            val date = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"

            val types = listOf(
                "Music", "Party", "Toilet", "Sport", "Art"
            )

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

    fun addEvent(event: Event) {
        val currentEvents = _events.value
        var updatedEvents = currentEvents?.plus(event)
        if (updatedEvents != null) {
            updatedEvents = updatedEvents.reversed()
        }
        _events.value = updatedEvents // Update the LiveData
    }

}

