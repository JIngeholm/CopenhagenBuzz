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

package dk.itu.moapd.copenhagenbuzz.jing.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.activityViewModels
import dk.itu.moapd.copenhagenbuzz.jing.adapters.TimeLineAdapter
import dk.itu.moapd.copenhagenbuzz.jing.objects.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentTimelineBinding
import com.firebase.ui.database.FirebaseListOptions
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.database
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.EventRowItemBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel

/**
 * A fragment that displays a timeline of events.
 *
 * [TimelineFragment] manages the UI for viewing events in a timeline format. It includes
 * a button for adding new events, which is only visible when the user is logged in.
 * The login state and event data are managed via [DataViewModel].
 */
class TimelineFragment : Fragment() {

    /**
     * View binding for the fragment's layout.
     */
    private var _binding: FragmentTimelineBinding? = null

    /**
     * Provides non-null access to the fragment's view binding.
     * Throws an exception if accessed when the view is not available.
     */
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    private var _eventBinding: EventRowItemBinding? = null

    private val eventBinding
        get() = requireNotNull(_eventBinding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    /**
     * Shared ViewModel instance for managing login state and event data.
     */
    private val dataViewModel: DataViewModel by activityViewModels()

    /**
     * Inflates the fragment's layout and initializes view binding.
     *
     * @param inflater The LayoutInflater used to inflate the layout.
     * @param container The parent view group, or null if not attached to a parent.
     * @param savedInstanceState A bundle containing any saved state.
     * @return The root view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called when the view for this fragment has been created.
     * This method is responsible for setting up various UI elements and observing data changes.
     *
     * It performs the following actions:
     * - Observes the login status (`isLoggedIn`) and toggles the visibility of the "Add Event" button.
     * - Sets up navigation to the event creation screen when the "Add Event" button is clicked.
     * - Observes the list of events and updates the list view with the new data.
     * - Maintains the scroll position of the list view even after it has been updated.
     * - Fetches and displays the latest events asynchronously.
     * - Saves the current scroll position and offset when the list is scrolled.
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState A bundle containing the state information of the fragment, if any.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataViewModel.auth.currentUser?.let {
            val query = database.reference
                .child("events")
                .orderByChild("eventStartDate")

            val options = FirebaseListOptions.Builder<Event>()
                .setQuery(query, Event::class.java)
                .setLayout(R.layout.event_row_item)
                .setLifecycleOwner(this)
                .build()

            // Create the custom adapter to bind a list of strings.
            val adapter = TimeLineAdapter(options,dataViewModel, parentFragmentManager)

            binding.listView.apply{
                this.adapter = adapter
            }
        }
    }

    /**
     * Cleans up the view binding reference when the fragment's view is destroyed
     * to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

