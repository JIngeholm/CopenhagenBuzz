import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputLayout

class SpinnerHelper {

    fun setUpSpinnerWithIcon(
        context: Context,
        spinner: Spinner,
        textInputLayout: TextInputLayout,
        items: Int,
        iconResId: Int
    ) {
        // Set up the spinner adapter
        val adapter = ArrayAdapter.createFromResource(
            context,
            items,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Load the icon using AppCompatResources
        val startIconDrawable = AppCompatResources.getDrawable(context, iconResId)

        // Set the icon on the TextInputLayout
        textInputLayout.startIconDrawable = startIconDrawable

        // Optional: Set padding and other styling if needed
        spinner.setPadding(48, 0, 16, 0)  // Adjust padding as needed
    }
}
