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

package dk.itu.moapd.copenhagenbuzz.jing

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class MyApplication: Application() {

    companion object {
        lateinit var DATABASE_URL: String
        lateinit var database: FirebaseDatabase
        lateinit var storage: FirebaseStorage
    }

    override fun onCreate() {
        super.onCreate()

        // Apply dynamic colors to activities if available
        DynamicColors.applyToActivitiesIfAvailable(this)

        DATABASE_URL = BuildConfig.DATABASE_URL

        if (DATABASE_URL.isNullOrEmpty()) {
            throw IllegalArgumentException("DATABASE_URL is not set or is empty in the .env file!")
        }

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance(DATABASE_URL)

        // Enable offline capabilities
        database.setPersistenceEnabled(true)

        // Connect to the Firebase Realtime Database
        val databaseReference = database.reference

        // Keep data synced offline
        databaseReference.keepSynced(true)

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()

        storage.maxDownloadRetryTimeMillis = 60000 // 1 minute retry time
        storage.maxOperationRetryTimeMillis = 60000
    }
}



