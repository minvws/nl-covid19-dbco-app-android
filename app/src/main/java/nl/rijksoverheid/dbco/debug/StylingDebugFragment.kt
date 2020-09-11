package nl.rijksoverheid.dbco.debug

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentStylingBinding


class StylingDebugFragment : BaseFragment(R.layout.fragment_styling) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentStylingBinding.bind(view)
        binding.toolbar.title = "Debug Styling Page"

    }
}