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
 * This activity handles event creation, including input for event details such as name, location,
 * date range, type, and description. It utilizes MaterialDatePicker for selecting event dates.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View binding instance for accessing UI components.
     */
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var isLoggedIn: Boolean = false

    /**
     * Companion object to store a static tag for logging.
     */
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    /**
     * Called when the activity is first created.
     * Initializes the UI and sets up event listeners.
     * Shares the boolean isLoggedIn with a shared ViewModel class.
     *
     * @param savedInstanceState A [Bundle] containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        val dataViewModel = ViewModelProvider(this)[DataViewModel::class.java]
        dataViewModel.setContext(this@MainActivity)
        dataViewModel.isLoggedIn.value = isLoggedIn

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fragment container handling_______________________________//

        val navHostFragment = supportFragmentManager
            .findFragmentById(
                R.id.fragment_container_view
            ) as NavHostFragment
        val navController = navHostFragment.navController

        //___________________________________________________________//

        // Menu handling____________________________________________//

        // Only use top appbar if orientation is portrait mode.
        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            setSupportActionBar(binding.topAppBar)
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)
        }

        binding.bottomNavigation?.setupWithNavController(navController)

        //___________________________________________________________//
    }

    /**
     * Handles the Up navigation action in the app bar.
     *
     * This method allows the user to navigate up in the app's navigation hierarchy.
     * If the navigation action cannot be performed, the method falls back to the default behavior.
     *
     * @return Boolean value indicating whether the navigation action was handled.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container_view)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Called to create the options menu for the activity.
     *
     * This method inflates the top app bar menu and adjusts visibility of menu items
     * based on the user's login state.
     *
     * @param menu The menu to be inflated.
     * @return Boolean value indicating whether the menu was successfully created.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_navigation_menu, menu)
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)

        menu?.findItem(R.id.action_login)?.isVisible = !isLoggedIn
        menu?.findItem(R.id.action_logout)?.isVisible = isLoggedIn
        return true
    }

    /**
     * Handles menu item selections for login and logout actions.
     *
     * When the login action is selected, the LoginActivity is started. When the logout action
     * is selected, the user is logged out, and the app navigates to the login screen.
     * The back stack is cleared to prevent navigating back to the main activity after logout.
     *
     * @param item The menu item that was selected.
     * @return Boolean value indicating whether the item selection was handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.action_logout -> {
                isLoggedIn = false
                invalidateOptionsMenu()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

