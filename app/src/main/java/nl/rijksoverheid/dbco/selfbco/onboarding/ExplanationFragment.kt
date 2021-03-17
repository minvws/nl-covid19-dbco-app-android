/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.onboarding

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentExplanationBinding

class ExplanationFragment : BaseFragment(R.layout.fragment_explanation) {

    val args: ExplanationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentExplanationBinding.bind(view)

        binding.onboardingHeader.text = if (args.flow == REGULAR_FLOW) {
            getString(R.string.normal_flow_explanation_title)
        } else {
            getString(R.string.selfbco_explanation_title)
        }

        binding.onboardingSubtext.text = if (args.flow == REGULAR_FLOW) {
            getString(R.string.normal_flow_explanation_summary)
        } else {
            getString(R.string.selfbco_explanation_summary)
        }

        binding.btnNext.setOnClickListener {
            if (args.flow == REGULAR_FLOW) {
                findNavController().navigate(ExplanationFragmentDirections.toCodeFillFragment())
            } else {
                findNavController().navigate(ExplanationFragmentDirections.toPrivacyConsentFragment())
            }
        }

        binding.backButton.setOnClickListener { findNavController().popBackStack() }
    }

    companion object {

        const val REGULAR_FLOW = 0
        const val SELF_BCO_FLOW = 1
    }
}