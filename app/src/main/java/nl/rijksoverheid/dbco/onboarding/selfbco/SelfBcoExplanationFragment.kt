/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding.selfbco

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentOnboardingAddDataBinding
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoExplanationBinding
import nl.rijksoverheid.dbco.onboarding.FillCodeFragment
import nl.rijksoverheid.dbco.util.hideKeyboard

class SelfBcoExplanationFragment : BaseFragment(R.layout.fragment_selfbco_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoExplanationBinding.bind(view)
        binding.btnNext.setOnClickListener {
            //findNavController().navigate(OnboardingAddDataFragmentDirections.toMyContactsFragment())
        }

        binding.backButton.setOnClickListener {
            it.hideKeyboard()
            it.postDelayed({
                findNavController().popBackStack()
            }, FillCodeFragment.KEYBOARD_DELAY)
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // you can't go back from this fragment, only close the app
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}