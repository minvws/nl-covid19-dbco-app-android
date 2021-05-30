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
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentOnboardingExplanationBinding
import nl.rijksoverheid.dbco.util.HtmlHelper

class OnboardingExplanationFragment : BaseFragment(R.layout.fragment_onboarding_explanation) {

    val args: OnboardingExplanationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingExplanationBinding.bind(view)

        binding.onboardingHeader.text = if (args.flow == REGULAR_FLOW) {
            getString(R.string.normal_flow_explanation_title)
        } else {
            getString(R.string.selfbco_explanation_title)
        }

        val summary = when (args.flow) {
            REGULAR_FLOW -> getString(R.string.normal_flow_explanation_summary)
            SELF_BCO_FLOW_SUPPORTED_REGION -> getString(R.string.selfbco_explanation_supported_summary)
            else -> getString(R.string.selfbco_explanation_unsupported_summary)
        }
        binding.onboardingSubtext.text = HtmlHelper.buildSpannableFromHtml(
            summary,
            requireContext()
        )

        with(binding.btnNext) {
            text = if (args.flow == SELF_BCO_FLOW_UNSUPPORTED_REGION) {
                getString(R.string.selfbco_explanation_unsupported_button)
            } else {
                getString(R.string.next)
            }
            setOnClickListener {
                if (args.flow == REGULAR_FLOW) {
                    findNavController().navigate(OnboardingExplanationFragmentDirections.toCodeFillFragment())
                } else {
                    findNavController().navigate(
                        OnboardingExplanationFragmentDirections.toPrivacyConsentFragment(
                            canGoBack = true
                        )
                    )
                }
            }
        }

        binding.toolbar.backButton.setOnClickListener { findNavController().popBackStack() }
    }

    companion object {

        const val REGULAR_FLOW = 0
        const val SELF_BCO_FLOW_SUPPORTED_REGION = 1
        const val SELF_BCO_FLOW_UNSUPPORTED_REGION = 2
    }
}