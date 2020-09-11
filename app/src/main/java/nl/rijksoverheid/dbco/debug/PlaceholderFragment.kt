package nl.rijksoverheid.dbco.debug

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_placeholder.*
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R

class PlaceholderFragment : BaseFragment(R.layout.fragment_placeholder){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_styling.setOnClickListener {
            findNavController().navigate(PlaceholderFragmentDirections.toStylingFragment())
        }
    }
}