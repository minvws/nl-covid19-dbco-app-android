/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentOnboardingFlowSelectionBinding

class OnboardingFlowSelectionFragment : BaseFragment(R.layout.fragment_onboarding_flow_selection) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingFlowSelectionBinding.bind(view)

        binding.btnNext.setOnClickListener {
            findNavController().navigate(
                OnboardingFlowSelectionFragmentDirections.toCodeFillFragment()
            )
        }

        binding.btnNoCode.setOnClickListener {
            findNavController().navigate(
                OnboardingFlowSelectionFragmentDirections.toSelfBcoExplanationFragment()
            )
        }

        binding.toolbar.backButton.setOnClickListener { findNavController().popBackStack() }
    }
}