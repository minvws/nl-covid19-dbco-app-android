/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

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

        binding.buttonAbout.setOnClickListener {
            findNavController().navigate(PlaceholderFragmentDirections.toAboutFragment())
        }

        binding.buttonMyContacts.setOnClickListener {
            findNavController().navigate(PlaceholderFragmentDirections.toMyContactsFragment())
        }
    }
}