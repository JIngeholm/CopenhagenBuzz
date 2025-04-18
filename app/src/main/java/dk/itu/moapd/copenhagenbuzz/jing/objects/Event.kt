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

package dk.itu.moapd.copenhagenbuzz.jing.objects

/**
 * @constructor A constructor for a kotlin data class for social events.
 * @param[eventName] the name of the event of type string.
 * @param[eventLocation] the location of the event of type EventLocation.
 * @param[eventStartDate] the start date of the event of type string.
 * @param[eventEndDate] the end date of the event of type string.
 * @param[eventType] the type of event of type string.
 * @param[eventDescription] the description of the event of type string.
 * @author Johan Ingeholm
 */
data class Event(
    var eventName: String = "",
    var eventLocation: EventLocation = EventLocation(),
    var eventStartDate: String = "",
    var eventEndDate: String = "",
    var eventType: String = "",
    var eventDescription: String = "",
    var eventPhoto: String = "",
    var userId: String = "",
    var eventID: String = "",
    var favoritedBy: MutableMap<String, Boolean> = mutableMapOf(),
    var invitedUsers: Map<String, String> = emptyMap()
) {
    constructor() : this("", EventLocation(),"","","","","","","")
}
