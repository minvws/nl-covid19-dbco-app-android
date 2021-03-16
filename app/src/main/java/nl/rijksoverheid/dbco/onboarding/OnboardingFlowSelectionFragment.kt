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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleViewModel
import nl.rijksoverheid.dbco.databinding.FragmentOnboardingFlowSelectionBinding
import nl.rijksoverheid.dbco.selfbco.onboarding.ExplanationFragment

class OnboardingFlowSelectionFragment : BaseFragment(R.layout.fragment_onboarding_flow_selection) {

    private val viewModel by viewModels<OnboardingHelpViewModel>()

    private val appLifecycleViewModel by viewModels<AppLifecycleViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingFlowSelectionBinding.bind(view)
        binding.viewmodel = appLifecycleViewModel

        if (viewModel.skipOnboarding) {
            findNavController().navigate(OnboardingFlowSelectionFragmentDirections.toMyContacts())
        }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(
                OnboardingFlowSelectionFragmentDirections.toExplanationFragment(
                    ExplanationFragment.REGULAR_FLOW
                )
            )
        }

        if (appLifecycleViewModel.getFeatureFlags().enableSelfBCO) { // TODO ??
            binding.btnNoCode.setOnClickListener {
                findNavController().navigate(
                    OnboardingFlowSelectionFragmentDirections.toExplanationFragment(
                        ExplanationFragment.SELF_BCO_FLOW
                    )
                )
            }
        } else {
            binding.btnNoCode.isVisible = false
        }
    }
}