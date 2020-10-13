/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentFillCodeBinding
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.showKeyboard
import org.jetbrains.annotations.NotNull

/**
 * TODO add ViewModel with API call
 */
class FillCodeFragment : BaseFragment(R.layout.fragment_fill_code) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFillCodeBinding.bind(view)

        binding.backButton.setOnClickListener {
            it.hideKeyboard()
            it.postDelayed({
                findNavController().popBackStack()
            }, KEYBOARD_DELAY)
        }

        binding.btnNext.setOnClickListener {
            it.hideKeyboard()
            it.postDelayed({
                findNavController().navigate(FillCodeFragmentDirections.toOnboardingAddDataFragment())
            }, KEYBOARD_DELAY)
        }

        setupEntriesBehaviour(binding)

        // show keyboard for first field with some delay
        binding.pinEntry1.postDelayed({
            binding.pinEntry1.requestFocus()
            binding.pinEntry1.showKeyboard()
        }, KEYBOARD_DELAY)
    }

    private fun setupEntriesBehaviour(binding: @NotNull FragmentFillCodeBinding) {
        binding.pinEntry1.setOnPinEnteredListener {
            binding.pinEntry2.requestFocus()
        }

        binding.pinEntry2.setOnPinEnteredListener {
            binding.pinEntry3.requestFocus()
        }

        listOf(binding.pinEntry1, binding.pinEntry2, binding.pinEntry3).forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    binding.btnNext.isEnabled = binding.pinEntry1.text?.length == ENTRY_MAX_LENGTH
                            && binding.pinEntry2.text?.length == ENTRY_MAX_LENGTH
                            && binding.pinEntry3.text?.length == ENTRY_MAX_LENGTH

                    if (text?.length == 0) { // handle backspace button
                        when (it.id) {
                            R.id.pin_entry_3 -> binding.pinEntry2.requestFocus()
                            R.id.pin_entry_2 -> binding.pinEntry1.requestFocus()
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })
        }
    }

    companion object {
        const val ENTRY_MAX_LENGTH = 3
        const val KEYBOARD_DELAY = 400L
    }
}