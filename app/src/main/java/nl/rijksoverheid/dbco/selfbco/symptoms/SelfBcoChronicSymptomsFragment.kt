/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.symptoms

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoChronicSymptomsBinding
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel

class SelfBcoChronicSymptomsFragment : BaseFragment(R.layout.fragment_selfbco_chronic_symptoms) {

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoChronicSymptomsBinding.bind(view)

        val symptomOnsetDate =
            selfBcoViewModel.getDateOfSymptomOnset().toString(DateFormats.selfBcoDateCheck)

        binding.datecheckSubtext.text = getString(
            R.string.selfbco_chronic_symptoms_summary,
            symptomOnsetDate
        )

        binding.btnEarlierTested.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoDateCheckFragment(
                    state = SelfBcoDateCheckState.createNegativeTestState(
                        context = requireContext(),
                        symptomOnsetDate = symptomOnsetDate
                    )
                )
            )
        }

        binding.btnAlwaysSymptoms.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoChronicSymptomsWorsenedFragment(
                    date = symptomOnsetDate
                )
            )
        }

        binding.btnBoth.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoDateCheckFragment(
                    state = SelfBcoDateCheckState.createNegativeTestWithChronicSymptomsState(
                        context = requireContext(),
                        symptomOnsetDate = symptomOnsetDate
                    )
                )
            )
        }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsFragmentDirections.toSelfBcoDoubleCheckFragment()
            )
        }

        binding.toolbar.backButton.setOnClickListener { findNavController().popBackStack() }
    }
}