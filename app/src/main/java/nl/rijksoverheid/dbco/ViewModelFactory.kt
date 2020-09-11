package nl.rijksoverheid.dbco

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val context = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            // Placeholder only
            else -> throw IllegalStateException("Unknown view model class $modelClass")
        }
    }
}