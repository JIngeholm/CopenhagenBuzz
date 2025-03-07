package dk.itu.moapd.copenhagenbuzz.jing

import android.app.Application
import com.google.android.material.color.DynamicColors
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic colors to activities if available.
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}