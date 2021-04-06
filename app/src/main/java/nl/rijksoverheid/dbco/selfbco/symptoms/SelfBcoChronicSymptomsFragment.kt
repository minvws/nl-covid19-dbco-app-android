/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.symptoms

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoChronicSymptomsBinding

class SelfBcoChronicSymptomsFragment : BaseFragment(R.layout.fragment_selfbco_chronic_symptoms) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoChronicSymptomsBinding.bind(view)

        binding.btnEarlierTested.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoDateCheckFragment(
                    state = SelfBcoDateCheckState.createNegativeTestState(requireContext())
                )
            )
        }

        binding.btnAlwaysSymptoms.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoChronicSymptomsWorsenedFragment()
            )
        }

        binding.btnBoth.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoDateCheckFragment(
                    state = SelfBcoDateCheckState.createNegativeTestWithChronicSymptomsState(
                        requireContext()
                    )
                )
            )
        }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoDoubleCheckFragment()
            )
        }

        binding.backButton.setOnClickListener { findNavController().popBackStack() }
    }
}