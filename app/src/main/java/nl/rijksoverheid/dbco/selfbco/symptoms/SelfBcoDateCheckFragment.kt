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
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBindingImpl
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import nl.rijksoverheid.dbco.selfbco.symptoms.SelfBcoDateCheckNavigation.*
import nl.rijksoverheid.dbco.util.getDate
import nl.rijksoverheid.dbco.util.hideKeyboard
import org.joda.time.LocalDate

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

        val state: SelfBcoDateCheckState = args.state

        binding.selfBcoDateHeader.text = state.title
        binding.selfBcoDateSummary.text = HtmlCompat.fromHtml(
            state.summary,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        val startDate = selfBcoViewModel.getStartDate()
        binding.datePicker.apply {
            updateDate(startDate.year, startDate.monthOfYear - 1, startDate.dayOfMonth)
        }

        binding.datePicker.maxDate = System.currentTimeMillis()

        binding.btnNext.setOnClickListener {
            val selectedDate = LocalDate(binding.datePicker.getDate().time)
            if (selfBcoViewModel.getTypeOfFlow() == SelfBcoConstants.SYMPTOM_CHECK_FLOW) {
                selfBcoViewModel.updateDateOfSymptomOnset(selectedDate)
            } else {
                selfBcoViewModel.updateDateOfTest(selectedDate)
            }
            handleNavigation(
                state.nextAction(
                    selectedDate,
                    LocalDate.now()
                )
            )
        }

        binding.btnInfo.setOnClickListener {
            findNavController().navigate(
                SelfBcoDateCheckFragmentDirections.toSelfBcoSymptomsExplanationFragment()
            )
        }

        binding.btnInfo.isVisible = state.showExplanation

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }
    }

    private fun handleNavigation(navigation: SelfBcoDateCheckNavigation) {
        val direction = when (navigation) {
            is PermissionCheck -> {
                SelfBcoDateCheckFragmentDirections.toSelfBcoPermissionFragment()
            }
            is SymptomDateDoubleCheck -> {
                SelfBcoDateCheckFragmentDirections.toSelfBcoDoubleCheckFragment()
            }
            is SymptomsWorsenedCheck -> {
                SelfBcoDateCheckFragmentDirections.toSelfBcoChronicSymptomsWorsenedFragment()
            }
            is ChronicSymptomCheck -> {
                SelfBcoDateCheckFragmentDirections.toSelfBcoChronicSymptomsFragment()
            }
        }
        findNavController().navigate(direction)
    }
}