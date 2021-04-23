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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentZipcodeSupportBinding
import nl.rijksoverheid.dbco.onboarding.OnboardingExplanationFragment
import nl.rijksoverheid.dbco.onboarding.FillCodeField
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.util.*

class ZipCodeSupportFragment : BaseFragment(R.layout.fragment_zipcode_support),
    FillCodeField.Callback {

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    private lateinit var binding: FragmentZipcodeSupportBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentZipcodeSupportBinding.bind(view)

        // Setup code entry
        binding.codeEntry.callback = this
        binding.codeEntry.showKeyboardWhenInPortrait(delay = KEYBOARD_DELAY)

        // Setup back button
        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }

        // Setup next button
        binding.nextButton.setOnClickListener {
            val direction =
                if (selfBcoViewModel.isZipCodeSupported(binding.codeEntry.code.toInt())) {
                    ZipCodeSupportFragmentDirections.toExplanationFragment(
                        OnboardingExplanationFragment.SELF_BCO_FLOW_SUPPORTED_REGION
                    )
                } else {
                    ZipCodeSupportFragmentDirections.toExplanationFragment(
                        OnboardingExplanationFragment.SELF_BCO_FLOW_UNSUPPORTED_REGION
                    )
                }
            findNavController().navigate(direction)
        }
    }

    override fun onTextChanged(field: FillCodeField, string: CharSequence?) {
        string?.let { text ->
            binding.nextButton.isEnabled = field.isFilled

            // Update placeholder
            if (text.length >= 0) {
                if (text.length <= PLACEHOLDER.length) {
                    binding.codePlaceholder.text = String.format(
                        "%s%s",
                        text,
                        PLACEHOLDER.subSequence(text.length, PLACEHOLDER.length)
                    )
                } else {
                    binding.codePlaceholder.text = text
                }
            } else {
                binding.codePlaceholder.text = PLACEHOLDER
            }
        }
    }

    companion object {
        private const val KEYBOARD_DELAY = 400L
        private const val PLACEHOLDER = "0000"
    }
}