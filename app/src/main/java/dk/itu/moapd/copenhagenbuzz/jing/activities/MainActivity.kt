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
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.jing.R
import dk.itu.moapd.copenhagenbuzz.jing.data.Event
import dk.itu.moapd.copenhagenbuzz.jing.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
    private lateinit var mainBinding: ActivityMainBinding

    private var isLoggedIn: Boolean = false

    /**
     * Companion object to store a static tag for logging.
     */
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    /**
     * Instance of [Event] used to store event details entered by the user.
     */
    private val event: Event = Event("", "", "", "", "")

    /**
     * Called when the activity is first created.
     * Initializes the UI and sets up event listeners.
     *
     * @param savedInstanceState A [Bundle] containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        invalidateOptionsMenu()
    }


}
