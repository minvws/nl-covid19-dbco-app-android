/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentOnboardingHelpBinding
import nl.rijksoverheid.dbco.storage.LocalStorageRepository

class OnboardingHelpFragment : BaseFragment(R.layout.fragment_onboarding_help) {

    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingHelpBinding.bind(view)
        binding.btnNext.setOnClickListener {
            findNavController().navigate(OnboardingHelpFragmentDirections.toFillCodeFragment())
        }

        encryptedSharedPreferences =
            LocalStorageRepository.getInstance(requireActivity()).getSharedPreferences()

        val savedPin = encryptedSharedPreferences.getString("pincode", null)
        if (savedPin != null) {
            findNavController().navigate(OnboardingHelpFragmentDirections.toMyContacts())
        }
    }

}