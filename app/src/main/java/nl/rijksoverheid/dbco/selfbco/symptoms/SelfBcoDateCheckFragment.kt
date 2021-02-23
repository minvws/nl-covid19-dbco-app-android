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
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBindingImpl
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import nl.rijksoverheid.dbco.util.getDate
import nl.rijksoverheid.dbco.util.hideKeyboard
import org.joda.time.DateTime

/**
 * Handles both date checking for testing and symptoms
 */
class SelfBcoDateCheckFragment : BaseFragment(R.layout.fragment_selfbco_date_check) {

    private val args: SelfBcoDateCheckFragmentArgs by navArgs()

    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoDateCheckBindingImpl.bind(view)

        when(args.dateCheckingFlow){
            SelfBcoConstants.SYMPTOM_CHECK_FLOW -> {
                binding.selfBcoDateHeader.text = getString(R.string.selfbco_date_symptoms_title)
                binding.selfBcoDateSummary.text = getString(R.string.selfbco_date_symptoms_summary)
                binding.datePicker.apply {
                    val date = args.date ?: selfBcoViewModel.getDateOfSymptomOnset()
                    updateDate(date.year, date.monthOfYear - 1, date.dayOfMonth)
                }
                selfBcoViewModel.setTypeOfFlow(SelfBcoConstants.SYMPTOM_CHECK_FLOW)
            }

            SelfBcoConstants.COVID_CHECK_FLOW -> {
                binding.selfBcoDateHeader.text = getString(R.string.selfbco_date_covid_title)
                binding.selfBcoDateSummary.text = getString(R.string.selfbco_date_covid_summary)
                selfBcoViewModel.setTypeOfFlow(SelfBcoConstants.COVID_CHECK_FLOW)
            }
        }

        binding.datePicker.maxDate = System.currentTimeMillis()

        binding.btnNext.setOnClickListener {
            val dateSelected = binding.datePicker.getDate()
            selfBcoViewModel.generateSelfBcoCase(DateTime(dateSelected.time))
            findNavController().navigate(SelfBcoDateCheckFragmentDirections.toSelfBcoDoubleCheckFragment(args.dateCheckingFlow, dateSelected.time))
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }

    }

}