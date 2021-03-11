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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBindingImpl
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import nl.rijksoverheid.dbco.util.getDate
import nl.rijksoverheid.dbco.util.hideKeyboard
import org.joda.time.LocalDate

/**
 * Handles both date checking for testing and symptoms
 */
class SelfBcoDateCheckFragment : BaseFragment(R.layout.fragment_selfbco_date_check) {

    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoDateCheckBindingImpl.bind(view)

        when (selfBcoViewModel.getTypeOfFlow()) {
            SelfBcoConstants.SYMPTOM_CHECK_FLOW -> {
                binding.selfBcoDateHeader.text = getString(R.string.selfbco_date_symptoms_title)
                binding.selfBcoDateSummary.text = getString(R.string.selfbco_date_symptoms_summary)
            }

            SelfBcoConstants.COVID_CHECK_FLOW -> {
                binding.selfBcoDateHeader.text = getString(R.string.selfbco_date_covid_title)
                binding.selfBcoDateSummary.text = getString(R.string.selfbco_date_covid_summary)
            }
        }

        val startDate = selfBcoViewModel.getStartDate()
        binding.datePicker.apply {
            updateDate(startDate.year, startDate.monthOfYear - 1, startDate.dayOfMonth)
        }

        binding.datePicker.maxDate = System.currentTimeMillis()

        binding.btnNext.setOnClickListener {
            val dateSelected = binding.datePicker.getDate()
            when (selfBcoViewModel.getTypeOfFlow()) {
                SelfBcoConstants.SYMPTOM_CHECK_FLOW -> {
                    selfBcoViewModel.updateDateOfSymptomOnset(LocalDate(dateSelected.time))
                }
                SelfBcoConstants.COVID_CHECK_FLOW -> {
                    selfBcoViewModel.updateDateOfTest(LocalDate(dateSelected.time))
                }
            }
            findNavController().navigate(
                SelfBcoDateCheckFragmentDirections.toSelfBcoDoubleCheckFragment()
            )
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }
    }
}