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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentFillCodeBinding
import nl.rijksoverheid.dbco.util.*
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingStatus.PairingError
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingStatus.PairingSuccess
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingStatus.PairingInvalid

class FillCodeFragment : BaseFragment(R.layout.fragment_fill_code), FillCodeField.Callback {

    private val viewModel: PairingViewModel by viewModels()

    private lateinit var binding: FragmentFillCodeBinding

    private var progressDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFillCodeBinding.bind(view)

        // Setup code entry
        binding.codeEntry.callback = this
        binding.codeEntry.showKeyboardWhenInPortrait(delay = KEYBOARD_DELAY)

        // Setup back button
        binding.backButton.setOnClickListener {
            it.hideKeyboard()
            it.postDelayed({
                findNavController().popBackStack()
            }, KEYBOARD_DELAY)
        }

        // Setup next button
        binding.nextButton.setOnClickListener {
            binding.nextButton.isEnabled = false
            progressDialog = showProgressDialog(R.string.pairing_code_being_checked)
            viewModel.pair(binding.codeEntry.code)
        }

        // Setup pairing logic
        viewModel.pairingStatus.observe(viewLifecycleOwner, { result ->
            progressDialog?.dismiss()
            when (result) {
                is PairingSuccess -> {
                    binding.nextButton.isEnabled = true
                    binding.nextButton.hideKeyboard()
                    binding.nextButton.postDelayed({
                        findNavController()
                            .navigate(
                                FillCodeFragmentDirections.toOnboardingPrivacyConsentFragment(
                                    canGoBack = false
                                )
                            )
                    }, KEYBOARD_DELAY)
                }
                is PairingInvalid -> {
                    binding.inputErrorView.setText(R.string.fill_code_invalid)
                    binding.inputErrorView.accessibilityAnnouncement(R.string.fill_code_invalid)
                    binding.inputErrorView.visibility = View.VISIBLE
                    binding.scrollView.scrollTo(binding.inputErrorView)
                    binding.nextButton.isEnabled = true
                }
                is PairingError -> {
                    binding.inputErrorView.setText(R.string.error_while_pairing)
                    showErrorDialog(getString(R.string.error_while_pairing), {
                        binding.codeEntry.requestFocus()
                    }, result.exception)
                    binding.inputErrorView.visibility = View.VISIBLE
                    binding.scrollView.scrollTo(binding.inputErrorView)
                    binding.nextButton.isEnabled = true
                }
            }
        })
    }

    override fun onTextChanged(field: FillCodeField, string: CharSequence?) {
        string?.let { text ->
            binding.nextButton.isEnabled = field.isFilled

            if (binding.inputErrorView.visibility == View.VISIBLE) {
                binding.inputErrorView.visibility = View.GONE
            }

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
        private const val PLACEHOLDER = "0000-0000-0000"
    }
}