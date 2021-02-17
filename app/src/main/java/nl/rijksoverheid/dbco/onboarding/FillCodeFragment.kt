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
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentFillCodeBinding
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.resolve
import nl.rijksoverheid.dbco.util.showKeyboard
import nl.rijksoverheid.dbco.util.updateText
import retrofit2.HttpException

class FillCodeFragment : BaseFragment(R.layout.fragment_fill_code), FillCodeField.Callback {

    private val viewModel by viewModels<FillCodeViewModel>()
    private lateinit var binding: FragmentFillCodeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFillCodeBinding.bind(view)

        binding.backButton.setOnClickListener {
            it.hideKeyboard()
            it.postDelayed({
                findNavController().popBackStack()
            }, KEYBOARD_DELAY)
        }

        binding.nextButton.setOnClickListener {
            viewModel.pair(binding.codeEntry.code)
            binding.nextButton.isEnabled = false
            binding.loadingContainer.visibility = View.VISIBLE
            binding.loadingIndicator.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }

        binding.codeEntry.callback = this

        binding.codeEntry.postDelayed({
            binding.codeEntry.requestFocus()
            binding.codeEntry.showKeyboard()
        }, KEYBOARD_DELAY)

        viewModel.pairingResult.observe(viewLifecycleOwner, { resource ->
            resource?.resolve(onError = { exception ->
                if (exception is HttpException && exception.code() == 400) {
                    // Invalid pairing code, show error message but keep code
                    binding.inputErrorView.visibility = View.VISIBLE
                } else {
                    // Other general error
                    showErrorDialog(getString(R.string.error_while_pairing), {
                        binding.codeEntry.updateText("")
                        binding.codeEntry.requestFocus()
                    }, exception)
                }

                binding.nextButton.isEnabled = true
                binding.loadingContainer.visibility = View.GONE
            }, onSuccess = {
                binding.nextButton.isEnabled = true
                binding.loadingContainer.visibility = View.GONE
                binding.nextButton.hideKeyboard()
                binding.nextButton.postDelayed({
                    findNavController().navigate(FillCodeFragmentDirections.toOnboardingAddDataFragment())
                }, KEYBOARD_DELAY)
            })
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
                    binding.codePlaceholder.text = String.format("%s%s", text, PLACEHOLDER.subSequence(text.length, PLACEHOLDER.length))
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