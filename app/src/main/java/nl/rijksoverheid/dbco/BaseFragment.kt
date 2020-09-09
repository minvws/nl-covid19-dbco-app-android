package nl.rijksoverheid.dbco

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

abstract class BaseFragment @JvmOverloads constructor(
    @LayoutRes layout: Int,
    private val factoryProducer: (() -> ViewModelProvider.Factory)? = null
) : Fragment(layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return factoryProducer?.invoke() ?: ViewModelFactory(requireContext().applicationContext)
    }
}