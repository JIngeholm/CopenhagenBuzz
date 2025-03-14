package dk.itu.moapd.copenhagenbuzz.jing.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.copenhagenbuzz.jing.R
import com.google.android.material.snackbar.Snackbar

import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.FirebaseAuthUIAuthenticationResult


class LoginActivity : AppCompatActivity() {

    // Register the result launcher for Firebase Auth UI
    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            onSignInResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Make sure you have a layout set here
        createSignInIntent()
    }

    private fun createSignInIntent() {
        // Choose authentication providers.
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build() // You can add other providers as well.
        )

        // Create and launch the sign-in intent.
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setLogo(R.drawable.baseline_firebase_24) // Set your logo
            .setTheme(R.style.Theme_FirebaseAuthentication) // Make sure you have this theme in your styles.xml
            .apply {
                setTosAndPrivacyPolicyUrls(
                    "https://firebase.google.com/terms/",
                    "https://firebase.google.com/policies/…"
                )
            }
            .build()

        // Launch the sign-in activity
        signInLauncher.launch(signInIntent)
    }

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

    private fun startMainActivity() {
        // Start MainActivity after successful login
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish() // Close LoginActivity so the user cannot return here
        }
    }

    private fun showSnackBar(message: String) {
        // Assuming you have a SnackBar to show feedback
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
