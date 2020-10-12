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


class FillCodeFragment : BaseFragment(R.layout.fragment_fill_code) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFillCodeBinding.bind(view)
        binding.btnNext.setOnClickListener {
            findNavController().navigate(FillCodeFragmentDirections.toOnboardingAddDataFragment())
        }

        binding.pinEntry1.setOnPinEnteredListener {
            binding.pinEntry2.requestFocus()
        }

        binding.pinEntry2.setOnPinEnteredListener {
            binding.pinEntry3.requestFocus()
        }

        binding.pinEntry3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnNext.isEnabled = text?.length == 3
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }
}