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
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoExplanationBinding
import nl.rijksoverheid.dbco.onboarding.OnboardingExplanationFragment
import nl.rijksoverheid.dbco.util.*

class SelfBcoExplanationFragment : BaseFragment(R.layout.fragment_selfbco_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoExplanationBinding.bind(view)


        // Setup back button
        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }

        // Setup next button
        binding.btnNext.setOnClickListener {
            findNavController().navigate(
                SelfBcoExplanationFragmentDirections.toExplanationFragment(
                    OnboardingExplanationFragment.SELF_BCO_FLOW
                )
            )
        }
    }
}