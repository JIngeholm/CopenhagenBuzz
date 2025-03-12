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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.jing.models.DataViewModel


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
     * Configuration for the AppBar to manage navigation.
     */
    private lateinit var appBarConfiguration: AppBarConfiguration

    /**
     * Indicates whether the user is logged in.
     */
    private var isLoggedIn: Boolean = false

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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        val dataViewModel = ViewModelProvider(this)[DataViewModel::class.java]
        dataViewModel.isLoggedIn.value = isLoggedIn

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupAuthButtons()
    }

    /**
     * Configures navigation components based on the screen orientation.
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setSupportActionBar(binding.topAppBar)
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)

            binding.bottomNavigation?.setupWithNavController(navController)
        } else {
            binding.navigationRail?.setupWithNavController(navController)
        }
    }

    /**
     * Dynamically adjusts padding for the navigation rail in landscape mode.
     */
    private fun adjustNavigationPadding() {
        val screenHeight = resources.displayMetrics.heightPixels
        val padding = screenHeight / 6
        binding.navigationRail?.setPadding(
            binding.navigationRail!!.paddingLeft,
            padding,
            binding.navigationRail!!.paddingRight,
            padding
        )
    }

    /**
     * Configures login and logout buttons in landscape mode.
     */
    private fun setupAuthButtons() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.login?.visibility = if (!isLoggedIn) View.VISIBLE else View.GONE
            binding.logout?.visibility = if (isLoggedIn) View.VISIBLE else View.GONE

            binding.login?.setOnClickListener {
                navigateToLogin()
            }

            binding.logout?.setOnClickListener {
                isLoggedIn = false
                invalidateOptionsMenu()
                navigateToLogin()
            }
        }
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

    /**
     * Creates the options menu for the activity.
     *
     * Inflates the appropriate menu based on screen orientation and adjusts visibility of
     * login/logout menu items.
     *
     * @param menu The menu to be inflated.
     * @return `true` if the menu was successfully created.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            menuInflater.inflate(R.menu.bottom_navigation_menu, menu)
            menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        } else {
            menuInflater.inflate(R.menu.side_navigation_menu, menu)
        }

        menu?.findItem(R.id.action_login)?.isVisible = !isLoggedIn
        menu?.findItem(R.id.action_logout)?.isVisible = isLoggedIn
        return true
    }

    /**
     * Handles menu item selections, including login and logout actions.
     *
     * If the user selects login, the LoginActivity is started. If logout is selected,
     * the user is logged out, and the app navigates to the login screen, clearing the back stack.
     *
     * @param item The selected menu item.
     * @return `true` if the action was handled, otherwise calls the superclass implementation.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                navigateToLogin()
                true
            }
            R.id.action_logout -> {
                isLoggedIn = false
                invalidateOptionsMenu()
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}


