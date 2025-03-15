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
import dk.itu.moapd.copenhagenbuzz.jing.R
import com.google.android.material.snackbar.Snackbar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

/**
 * `LoginActivity` is an activity that handles user authentication using Firebase Authentication.
 * It provides options for users to sign in with email, Google, or as a guest (anonymous).
 * Upon successful authentication, the user is redirected to the `MainActivity`.
 *
 * This activity uses Firebase UI Auth to simplify the authentication process and provides
 * a seamless user experience for logging in.
 *
 * @see AppCompatActivity
 */
class LoginActivity : AppCompatActivity() {

    /**
     * A result launcher for Firebase Auth UI, used to handle the result of the authentication flow.
     * The result is processed in the `onSignInResult` method.
     */
    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            onSignInResult(result)
        }

    /**
     * Called when the activity is created. This method initializes the activity and starts
     * the authentication process by calling `createSignInIntent`.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down, this Bundle contains the data it most recently supplied in `onSaveInstanceState`.
     * Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSignInIntent()
    }

    /**
     * Creates and launches the sign-in intent using Firebase Auth UI.
     * This method configures the available authentication providers (email, Google, and anonymous)
     * and sets up the sign-in UI with a custom logo and theme.
     *
     * The user can choose their preferred authentication method, and the result is handled by
     * the `signInLauncher`.
     */
    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), // Email provider
            AuthUI.IdpConfig.GoogleBuilder().build(), // Google provider
            AuthUI.IdpConfig.AnonymousBuilder().build() // Guest (Anonymous) provider
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setLogo(R.drawable.baseline_firebase_24) // Set your logo
            .setTheme(R.style.Theme_FirebaseAuthentication) // Set your theme
            .apply {
                setTosAndPrivacyPolicyUrls(
                    "https://firebase.google.com/terms/",
                    "https://firebase.google.com/policies/…"
                )
            }
            .build()

        signInLauncher.launch(signInIntent)
    }

    /**
     * Handles the result of the authentication flow.
     * If the authentication is successful, the user is redirected to `MainActivity`.
     * If the authentication fails, a Snackbar is displayed to notify the user.
     *
     * @param result The result of the authentication flow, containing the result code and data.
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                // Successfully signed in.
                showSnackBar("User logged in the app.")
                startMainActivity()
            }
            else -> {
                // Sign in failed.
                showSnackBar("Authentication failed.")
            }
        }
    }

    /**
     * Starts the `MainActivity` and passes a boolean value (`isGuest`) to indicate whether
     * the user is logged in with a permanent account (email or Google) or as a guest (anonymous).
     * The `LoginActivity` is finished so the user cannot navigate back to it.
     */
    private fun startMainActivity() {
        // Start MainActivity after successful login
        Intent(this, MainActivity::class.java).apply {
            // Add the isLoggedIn boolean to the intent
            val isGuest = FirebaseAuth.getInstance().currentUser?.isAnonymous == true
            putExtra("isGuest", isGuest) // Key-value pair
            startActivity(this)
            finish() // Close LoginActivity so the user cannot return here
        }
    }

    /**
     * Displays a Snackbar with the given message.
     * This method is used to provide feedback to the user, such as success or failure messages.
     *
     * @param message The message to display in the Snackbar.
     */
    private fun showSnackBar(message: String) {
        // Assuming you have a SnackBar to show feedback
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}