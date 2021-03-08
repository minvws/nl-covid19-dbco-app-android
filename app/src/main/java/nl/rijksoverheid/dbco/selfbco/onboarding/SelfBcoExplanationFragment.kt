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
import nl.rijksoverheid.dbco.util.hideKeyboard

class SelfBcoExplanationFragment : BaseFragment(R.layout.fragment_selfbco_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoExplanationBinding.bind(view)
        binding.btnNext.setOnClickListener {
            findNavController().navigate(SelfBcoExplanationFragmentDirections.toPrivacyConsentFragment())
        }

        binding.backButton.setOnClickListener {
            it.hideKeyboard()
            it.postDelayed({
                findNavController().popBackStack()
            }, KEYBOARD_DELAY)
        }

    }

    companion object {
        private const val KEYBOARD_DELAY = 400L
    }
}