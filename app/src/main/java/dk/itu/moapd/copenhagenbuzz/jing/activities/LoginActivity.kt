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
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dk.itu.moapd.copenhagenbuzz.jing.databinding.ActivityLoginBinding

/**
 * The LoginActivity class provides the UI for the user to log in or
 * access the application as a guest.
 */
class LoginActivity : AppCompatActivity() {

    /**
     * Binding object for the login activity layout.
     */
    private lateinit var mainBinding: ActivityLoginBinding

    companion object {
        /**
         * Log tag used for debugging purposes.
         */
        private val TAG = MainActivity::class.qualifiedName
    }

    /**
     * Called when the activity is starting. Initializes the view components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        initializeViews()
    }

    /**
     * Initializes the UI views and sets up click listeners for login and guest access.
     * Navigates to the main activity with the corresponding login status.
     */
    private fun initializeViews(){
        mainBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mainBinding.loginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("isLoggedIn", true)
            }
            startActivity(intent)
        }

        mainBinding.guestButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("isLoggedIn", false)
            }
            startActivity(intent)
        }
    }
}