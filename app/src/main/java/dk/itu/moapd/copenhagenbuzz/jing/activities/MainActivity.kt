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
 * This activity is responsible for managing the main user interface and navigation within the app.
 * It handles user authentication (login/logout) and adapts the UI based on whether the user is a guest or logged in.
 * The activity supports both portrait and landscape orientations, adjusting the navigation elements accordingly.
 *
 * It also manages user settings and interactions with the drawer menu, including displaying user-specific information
 * and handling user login/logout actions.
 *
 * @constructor Creates an instance of the main activity for the CopenhagenBuzz application.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View binding instance for accessing UI components.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * Indicates whether the current user is a guest or logged in.
     * This determines which content is displayed in the UI.
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
     * Initializes Firebase Auth, sets up the navigation components, and handles user login status.
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
     * Called when the activity starts.
     * Redirects the user to the LoginActivity if they are not logged in.
     */
    override fun onStart() {
        super.onStart()
        // Redirect the user to the LoginActivity if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }

    /**
     * Called when the activity is resumed.
     * Refreshes user information in the drawer header.
     */
    override fun onResume() {
        super.onResume()
        setupDrawerHeader()  // Refresh user info whenever activity is resumed
    }

    /**
     * Starts the LoginActivity.
     */
    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    /**
     * Sets up the navigation controller for the activity.
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
            if (destination.id == R.id.fragment_timeline) {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar?.setHomeAsUpIndicator(null)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    /**
     * Sets up the drawer menu, including navigation actions and visibility of items.
     */
    private fun setupDrawer() {
        // Show or hide user settings section based on login status
        binding.navigationView.menu.findItem(R.id.user_settings).isVisible = !isGuest

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.topAppBar?.setNavigationOnClickListener {
                binding.drawerLayout.open()
            }
        } else {
            binding.navButton?.setOnClickListener {
                binding.drawerLayout.open()
            }
        }

        // Setup header for drawer with account info
        setupDrawerHeader()

        // Actions for pressing menu items
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.new_event -> navigateToFragment(R.id.action_timeline_to_add_event)
                R.id.account -> navigateToFragment(R.id.action_timeline_to_account)
            }
            binding.drawerLayout.close()
            true
        }
    }

    /**
     * Sets up the header in the navigation drawer with user information.
     */
    fun setupDrawerHeader() {
        auth.currentUser?.reload()?.addOnCompleteListener {
            val user = auth.currentUser
            val headerView = binding.navigationView.getHeaderView(0)
            val loginButton = headerView?.findViewById<MaterialButton>(R.id.login_button)

            loginButton?.setOnClickListener { navigateToLogin() }

            if (isGuest) return@addOnCompleteListener

            val profileImageView = headerView?.findViewById<ImageView>(R.id.drawer_profile_photo)
            user?.let {
                Picasso.get()
                    .load(user.photoUrl)
                    .placeholder(R.drawable.baseline_account_circle_60)
                    .error(R.drawable.baseline_account_circle_60)
                    .into(profileImageView)

                headerView.findViewById<TextView>(R.id.drawer_username)?.text = user.displayName
                headerView.findViewById<TextView>(R.id.drawer_email)?.text = user.email
            }

            val loginTextRes = if (isGuest) R.string.sign_in else R.string.sign_out
            loginButton?.setText(loginTextRes)
        }
    }

    /**
     * Navigates to the specified fragment.
     */
    private fun navigateToFragment(destination: Int) {
        val navController = findNavController(R.id.fragment_container_view)
        val actionId = when (navController.currentDestination?.id) {
            R.id.fragment_timeline -> destination
            R.id.fragment_favorites -> destination
            R.id.fragment_maps -> destination
            R.id.fragment_calendar -> destination
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
     * Navigates to the LoginActivity.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Adds the current user to the Firebase Realtime Database if they are not already present.
     */
    private fun addUserToDB(fbUser: FirebaseUser) {
        val databaseReference = Firebase.database(DATABASE_URL).reference
        val userRef = databaseReference.child("users").child(fbUser.uid)

        // Check if the user already exists in the database
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("addUserToDB", "User already exists: ${fbUser.uid}")
                } else {
                    val newUser = buzzUser(
                        fbUser.displayName.toString(),
                        fbUser.email.toString(),
                        fbUser.photoUrl.toString(),
                        fbUser.uid
                    )

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
