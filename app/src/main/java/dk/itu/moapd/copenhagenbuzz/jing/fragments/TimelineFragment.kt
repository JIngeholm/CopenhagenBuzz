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
import android.widget.AbsListView
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentTimelineBinding
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

    /**
     * Shared ViewModel instance for managing login state and event data.
     */
    private val dataViewModel: DataViewModel by activityViewModels()

    // Variable to save the current scroll position
    var currentScrollPosition = 0

    private var currentScrollOffset: Int = 0

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
        // Observe the event list updates to refresh the adapter with new data.
        // This ensures that the displayed events are always up-to-date.
        dataViewModel.events.observe(viewLifecycleOwner) { events ->
            val adapter = binding.listView.adapter as? EventAdapter
            if (adapter == null) {
                // Set the adapter only if it hasn't been set yet.
                binding.listView.adapter = EventAdapter(requireContext(), dataViewModel, ArrayList(events ?: emptyList()))
            } else {
                // Update the existing adapter with the new event data.
                adapter.updateData(events ?: emptyList())
            }

            // Use postDelayed to ensure the scroll position is set after the list is rendered.
            // This ensures the list is scrolled to the previous position after it is updated.
            binding.listView.postDelayed({
                binding.listView.setSelectionFromTop(currentScrollPosition, currentScrollOffset)
            }, 100)  // Delay to ensure the list has been rendered
        }

        // Fetch and display the latest events asynchronously from the ViewModel.
        dataViewModel.loadEvents()

        // Save the current scroll position and offset when the list is scrolled.
        // This ensures the list maintains its position even after updates.
        binding.listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (view != null) {
                    // Save the first visible item and its offset to maintain the scroll position.
                    currentScrollPosition = firstVisibleItem
                    currentScrollOffset = view.getChildAt(0)?.top ?: 0
                }
            }
        })
    }




    /**
     * Navigates to the AddEventFragment based on the current navigation destination.
     * Ensures that the back stack is properly managed to prevent duplicate navigation.
     */
    private fun navigateToAddEvent() {
        val navController = findNavController()

        val actionId = when (navController.currentDestination?.id) {
            R.id.fragment_timeline -> R.id.action_timeline_to_add_event
            R.id.fragment_favorites -> R.id.action_favorites_to_add_event
            else -> return // Exit if the current destination is unknown.
        }

        navController.navigate(
            actionId,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false)
                .build()
        )
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

