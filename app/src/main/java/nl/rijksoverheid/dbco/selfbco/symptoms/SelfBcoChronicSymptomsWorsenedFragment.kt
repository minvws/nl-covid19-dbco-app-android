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
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoChronicSymptomsWorsenedBinding
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel

class SelfBcoChronicSymptomsWorsenedFragment :
    BaseFragment(R.layout.fragment_selfbco_chronic_symptoms_worsened) {

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoChronicSymptomsWorsenedBinding.bind(view)

        binding.datecheckHeader.text = String.format(
            getString(R.string.selfbco_chronic_symptoms_worsened_title),
            selfBcoViewModel.getDateOfSymptomOnset().toString(DateFormats.selfBcoDateCheck)
        )

        binding.btnNo.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsWorsenedFragmentDirections.toSelfBcoDateCheckFragment(
                    state = SelfBcoDateCheckState.createSymptomTestState(requireContext())
                )
            )
        }

        binding.btnYes.setOnClickListener {
            findNavController().navigate(
                SelfBcoChronicSymptomsWorsenedFragmentDirections.toSelfBcoDateCheckFragment(
                    state = SelfBcoDateCheckState.createSymptomIncreasedState(
                        context = requireContext(),
                        date = selfBcoViewModel
                            .getDateOfSymptomOnset()
                            .toString(DateFormats.selfBcoDateCheck)
                    )
                )
            )
        }

        binding.backButton.setOnClickListener { findNavController().popBackStack() }
    }
}