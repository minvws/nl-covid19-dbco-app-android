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
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDoublecheckBindingImpl
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants

class SelfBcoDoubleCheckFragment : BaseFragment(R.layout.fragment_selfbco_doublecheck) {

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoDoublecheckBindingImpl.bind(view)

        val flow = selfBcoViewModel.getTypeOfFlow()

        val newSymptomOnsetDate =
            if (flow == SelfBcoConstants.SYMPTOM_CHECK_FLOW) {
                selfBcoViewModel.getDateOfSymptomOnset().minusDays(1)
            } else {
                selfBcoViewModel.getDateOfTest()
            }

        when (flow) {
            SelfBcoConstants.SYMPTOM_CHECK_FLOW -> {
                binding.datecheckHeader.text = String
                    .format(
                        getString(R.string.selfbco_checkdate_symptoms_title),
                        newSymptomOnsetDate.toString(DateFormats.selfBcoDateCheck)
                    )
                binding.datecheckSubtext.text = getString(R.string.selfbco_checkdate_summary)
                binding.btnHadSymptoms.text = getString(R.string.selfbco_had_symptoms)
            }

            SelfBcoConstants.COVID_CHECK_FLOW -> {
                binding.datecheckHeader.text = getString(R.string.selfbco_checkdate_covid_title)
                binding.datecheckSubtext.text = getString(R.string.selfbco_checkdate_summary)
                binding.btnHadSymptoms.text = getString(R.string.selfbco_covid_had_symptoms)
            }
        }

        binding.btnHadSymptoms.setOnClickListener {
            selfBcoViewModel.setTypeOfFlow(SelfBcoConstants.SYMPTOM_CHECK_FLOW)
            selfBcoViewModel.updateDateOfSymptomOnset(newSymptomOnsetDate)
            val symptoms = selfBcoViewModel.getSelectedSymptomsSize()
            val direction = if (symptoms > 0) {
                SelfBcoDoubleCheckFragmentDirections.toSelfBcoDateCheckFragment(
                    state = SelfBcoDateCheckState.createSymptomState(requireContext())
                )
            } else {
                // select symptoms
                SelfBcoDoubleCheckFragmentDirections.toSymptomSelectionFragment()
            }
            findNavController().navigate(direction)
        }

        binding.btnNext.setOnClickListener {
            findNavController()
                .navigate(SelfBcoDoubleCheckFragmentDirections.toSelfBcoPermissionFragment())
        }

        binding.backButton.setOnClickListener { findNavController().popBackStack() }
    }
}