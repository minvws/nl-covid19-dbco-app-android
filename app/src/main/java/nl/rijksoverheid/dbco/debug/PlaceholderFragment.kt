package nl.rijksoverheid.dbco.debug

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentPlaceholderBinding

class PlaceholderFragment : BaseFragment(R.layout.fragment_placeholder){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPlaceholderBinding.bind(view)
        binding.buttonStyling.setOnClickListener {
            findNavController().navigate(PlaceholderFragmentDirections.toStylingFragment())
        }
    }
}