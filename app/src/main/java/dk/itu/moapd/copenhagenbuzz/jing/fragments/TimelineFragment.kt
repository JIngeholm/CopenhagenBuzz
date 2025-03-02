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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.jing.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import kotlinx.coroutines.launch

/**
 * Fragment responsible for displaying the timeline of events.
 *
 * The [TimelineFragment] handles the display of events on the timeline and includes a button
 * for navigating to an event creation screen. The visibility of the "Add Event" button is
 * controlled based on the user's login status, which is managed via the [DataViewModel].
 */
class TimelineFragment : Fragment() {

    /**
     * View binding for the timeline fragment layout.
     */
    private var _binding: FragmentTimelineBinding? = null

    /**
     * Provides access to the binding for the fragment's views.
     * Throws an exception if the binding is null, indicating that the view is not visible.
     */
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    /**
     * Shared ViewModel instance for managing login status across fragments.
     */
    private val dataViewModel: DataViewModel by viewModels()

    /**
     * Called to create the view for the fragment.
     *
     * @param inflater The LayoutInflater used to inflate the fragment's layout.
     * @param container The parent view group that the fragment's UI should be attached to.
     * @param savedInstanceState A bundle containing the fragment's previously saved state.
     * @return The root view for the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after the view has been created. Sets up the login state observer and button listener.
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState A bundle containing any saved state from a previous instance.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
            if (isLoggedIn) {
                binding.openAddEventFragmentButton.visibility = View.VISIBLE
            } else {
                binding.openAddEventFragmentButton.visibility = View.GONE
            }
        })

        binding.openAddEventFragmentButton.setOnClickListener {
            navigateToAddEvent()
        }

        // Observe the events LiveData
        dataViewModel.events.observe(viewLifecycleOwner) { events ->
            // Update the adapter with the new list of events
            val adapter = EventAdapter(requireContext(), ArrayList(events))
            binding.listView?.adapter = adapter
        }

        // Assuming this code is inside a Fragment or Activity
        lifecycleScope.launch {
            // Fetch the list of events
            val list = dataViewModel.fetchMockEvents() // This returns ArrayList<Event>

            // Create the adapter and pass the list
            val adapter = EventAdapter(requireContext(), list)

            // Set the adapter to the ListView
            binding.listView?.adapter = adapter
        }
    }

    /**
     * Navigates to the AddEventFragment. The navigation action depends on the current fragment.
     * The backstack is managed to avoid navigating back to the AddEventFragment.
     */
    private fun navigateToAddEvent() {
        binding.openAddEventFragmentButton.setOnClickListener{
            val navController = findNavController()

            val actionId = when (navController.currentDestination?.id) {
                R.id.fragment_timeline -> R.id.action_timeline_to_add_event
                R.id.fragment_favorites -> R.id.action_favorites_to_add_event
                else -> return@setOnClickListener // Exit if destination is unknown
            }

            navController.navigate(
                actionId,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, false)
                    .build()
            )
        }
    }

    /**
     * Called when the fragment's view is destroyed. Cleans up the binding to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
