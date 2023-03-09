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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.AppViewModel
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentOnboardingStartBinding
import nl.rijksoverheid.dbco.util.HtmlHelper

class OnboardingStartFragment : BaseFragment(R.layout.fragment_onboarding_start) {

    private val appViewModel: AppViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingStartBinding.bind(view)

        binding.onboardingStartSubtext.text = HtmlHelper.buildSpannableFromHtml(
            getString(R.string.onboarding_start_subtext),
            requireContext()
        )

        binding.btnNext.setOnClickListener {
            val direction = if (appViewModel.getFeatureFlags().enableSelfBCO) {
                OnboardingStartFragmentDirections.toOnboardingFlowSelectionFragment()
            } else {
                OnboardingStartFragmentDirections.toCodeFillFragment()
            }
            findNavController().navigate(direction)
        }
    }
}