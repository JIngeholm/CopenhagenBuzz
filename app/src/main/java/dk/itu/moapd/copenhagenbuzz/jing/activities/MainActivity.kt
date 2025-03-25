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

package dk.itu.moapd.copenhagenbuzz.jing.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.jing.MyApplication.Companion.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.jing.dialogs.InboxDialog
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.jing.objects.buzzUser


/**
 * Main activity for the CopenhagenBuzz application.
 *
 * This activity manages the navigation and UI components of the app, including handling
 * user authentication (login/logout) and menu visibility. It supports both portrait and
 * landscape orientations, adapting navigation elements accordingly.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View binding instance for accessing UI components.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * Indicates if the user is a guest.
     * This is used to determine whether user-specific information should be shown in the UI.
     */
    private var isGuest: Boolean = true

    /**
     * Firebase authentication instance for managing user authentication.
     */
    private lateinit var auth: FirebaseAuth

    /**
     * Companion object containing a static tag for logging.
     */
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    /**
     * Called when the activity is created.
     *
     * Initializes the UI, sets up navigation, and configures authentication-related UI elements.
     * Shares the `isLoggedIn` state with a shared ViewModel.
     *
     * @param savedInstanceState A [Bundle] containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()

        // Get guest status from intent and update ViewModel state
        isGuest = intent.getBooleanExtra("isGuest", true)
        val dataViewModel = ViewModelProvider(this)[DataViewModel::class.java]
        dataViewModel.isLoggedIn.value = isGuest
        dataViewModel.auth = auth

        // Set up the UI using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize navigation and drawer components
        setupNavigation()
        setupDrawer()

        binding.inboxButton.setOnClickListener {
            val inboxDialog = InboxDialog()
            inboxDialog.show(supportFragmentManager, "InboxDialog")  // Use supportFragmentManager
        }

        // Add user to db, if it's not already in it
        auth.currentUser?.let { addUserToDB(it) }
    }

    /**
     * Called when the activity is starting or restarting. This is where the activity
     * checks if the user is logged in. If the user is not logged in (i.e., `auth.currentUser` is null),
     * the user is redirected to the [LoginActivity].
     *
     * This method ensures that the user is authenticated before proceeding to the main functionality
     * of the app. If the user is not authenticated, they are taken to the login screen.
     *
     * @see android.app.Activity.onStart
     */
    override fun onStart() {
        super.onStart()
        // Redirect the user to the LoginActivity if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }

    /**
     * Starts the [LoginActivity] and clears the back stack, ensuring that the user cannot navigate back
     * to the previous activity using the back button.
     *
     * This method is typically called when the user is not logged in and needs to be redirected
     * to the login screen. The [Intent.FLAG_ACTIVITY_NEW_TASK] and [Intent.FLAG_ACTIVITY_CLEAR_TASK]
     * flags are used to clear the back stack, ensuring a clean navigation flow.
     *
     * @see android.content.Intent
     */
    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    /**
     * Configures navigation components based on the screen orientation.
     *
     * This method handles setting up the navigation bar and configuring the app to adapt to both
     * portrait and landscape orientations. It adjusts the visibility and configuration of
     * navigation elements like the AppBar and BottomNavigationView or NavigationRail.
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setSupportActionBar(binding.topAppBar)
            binding.topAppBar?.setOverflowIcon(null)

            binding.bottomNavigation?.setupWithNavController(navController)
        } else {
            binding.navigationRail?.setupWithNavController(navController)

        }

        // Ensure the correct icon appears on each fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.fragment_timeline) { // Change to your fragment ID
                supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar?.setHomeAsUpIndicator(null)
                supportActionBar?.setDisplayHomeAsUpEnabled(true) // Shows the back arrow
            }
        }
    }

    /**
     * Configures the navigation drawer, including handling the visibility of user-specific settings
     * and setting up actions for menu items.
     *
     * This method controls the opening of the drawer, the visibility of user settings based on
     * whether the user is a guest, and sets up the menu item actions such as navigating to the AddEvent
     * screen.
     */
    private fun setupDrawer() {
        // Show or hide user settings section based on login status
        binding.navigationView.menu.findItem(R.id.user_settings).isVisible = !isGuest

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Open the drawer when the menu button is pressed
            binding.topAppBar?.setNavigationOnClickListener {
                binding.drawerLayout.open()
            }
        }else{
            binding.navButton?.setOnClickListener {
                binding.drawerLayout.open()
            }
        }

        // Setup header for drawer with account info
        setupDrawerHeader()

        // Actions for pressing menu items
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.new_event -> navigateToAddEvent()
            }
            binding.drawerLayout.close()
            true
        }
    }

    /**
     * Sets up the drawer header with user information, such as the profile photo and username.
     *
     * This method also sets the login button's text and click listener based on the guest status.
     */
    private fun setupDrawerHeader() {
        val headerView = binding.navigationView.getHeaderView(0) // Get the first (and only) header view
        val loginButton = headerView?.findViewById<MaterialButton>(R.id.login_button)

        loginButton?.setOnClickListener {
            navigateToLogin()
        }

        if (isGuest) return

        val profileImageView = headerView?.findViewById<ImageView>(R.id.drawer_profile_photo)
        if (profileImageView == null) {
            Log.e("MainActivity", "profileImageView is null")
            return
        }

        // Use Picasso to load the profile photo
        Picasso.get()
            .load(auth.currentUser?.photoUrl) // Load user's photo
            .placeholder(R.drawable.baseline_account_circle_60) // Placeholder image
            .error(R.drawable.baseline_account_circle_60) // Error image
            .into(profileImageView) // Into the ImageView

        // Set username and email
        headerView.findViewById<TextView>(R.id.drawer_username)?.text = auth.currentUser?.displayName
        headerView.findViewById<TextView>(R.id.drawer_email)?.text = auth.currentUser?.email

        // Set login button text and click listener
        val loginTextRes = if (isGuest) R.string.sign_in else R.string.sign_out
        loginButton?.setText(loginTextRes)
    }

    /**
     * Navigates to the AddEventFragment based on the current navigation destination.
     * Ensures that the back stack is properly managed to prevent duplicate navigation.
     */
    private fun navigateToAddEvent() {
        val navController = findNavController(R.id.fragment_container_view)
        val actionId = when (navController.currentDestination?.id) {
            R.id.fragment_timeline -> R.id.action_timeline_to_add_event
            R.id.fragment_favorites -> R.id.action_favorites_to_add_event
            R.id.fragment_maps -> R.id.action_maps_to_add_event
            R.id.fragment_calendar -> R.id.action_calendar_to_add_event
            else -> return
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
     * Navigates to the login activity and clears the back stack.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun addUserToDB(fbUser: FirebaseUser) {
        val databaseReference = Firebase.database(DATABASE_URL).reference
        val userRef = databaseReference.child("users").child(fbUser.uid)

        // Check if the user already exists in the database
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User already exists in the database
                    Log.d("addUserToDB", "User already exists: ${fbUser.uid}")
                } else {
                    // User does not exist, add them to the database
                    val newUser = buzzUser(fbUser.displayName.toString(),fbUser.email.toString(),fbUser.photoUrl.toString(),fbUser.uid)

                    userRef.setValue(newUser)
                        .addOnSuccessListener {
                            Log.d("addUserToDB", "User added to database: ${fbUser.uid}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("addUserToDB", "Failed to add user to database", e)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("addUserToDB", "Database query failed", error.toException())
            }
        })
    }

}



