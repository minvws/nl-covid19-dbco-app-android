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
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBinding
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBindingImpl
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import nl.rijksoverheid.dbco.selfbco.symptoms.SelfBcoDateCheckNavigation.*
import nl.rijksoverheid.dbco.util.HtmlHelper
import nl.rijksoverheid.dbco.util.getDate
import nl.rijksoverheid.dbco.util.hideKeyboard
import org.joda.time.LocalDate
import java.io.Serializable

/**
 * Handles both date checking for testing and symptoms
 */
class SelfBcoDateCheckFragment : BaseFragment(R.layout.fragment_selfbco_date_check) {

    private lateinit var binding: FragmentSelfbcoDateCheckBinding

    private val args: SelfBcoDateCheckFragmentArgs by navArgs()

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoDateCheckBindingImpl.bind(view)

        val state: SelfBcoDateCheckState = args.state
        val date = State.fromBundle(savedInstanceState)?.date ?: selfBcoViewModel.getStartDate()

        initToolbar()
        initContent(state, date)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        getState()?.addToBundle(outState)
        super.onSaveInstanceState(outState)
    }

    private fun initToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }
    }

    private fun initContent(state: SelfBcoDateCheckState, date: LocalDate) {
        binding.selfBcoDateHeader.text = state.title
        binding.selfBcoDateSummary.text = HtmlHelper.buildSpannableFromHtml(
            state.summary,
            requireContext()
        )

        binding.datePicker.apply {
            updateDate(date.year, date.monthOfYear - 1, date.dayOfMonth)
        }

        binding.datePicker.maxDate = System.currentTimeMillis()

        binding.btnNext.setOnClickListener {
            val selectedDate = getState()!!.date
            if (selfBcoViewModel.getTypeOfFlow() == SelfBcoConstants.SYMPTOM_CHECK_FLOW) {
                selfBcoViewModel.updateDateOfSymptomOnset(selectedDate)
            } else {
                selfBcoViewModel.updateDateOfTest(selectedDate)
            }
            handleNavigation(state.nextAction(selectedDate, LocalDate.now()))
        }

        binding.btnInfo.setOnClickListener {
            findNavController().navigate(
                SelfBcoDateCheckFragmentDirections.toSelfBcoSymptomsExplanationFragment()
            )
        }

        binding.btnInfo.isVisible = state.showExplanation
    }

    private fun getState(): State? {
        return if (::binding.isInitialized) {
            State(date = LocalDate(binding.datePicker.getDate().time))
        } else null
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

    private data class State(
        val date: LocalDate
    ) : Serializable {

        fun addToBundle(bundle: Bundle) {
            bundle.putSerializable(STATE_KEY, this)
        }

        companion object {
            private const val STATE_KEY = "SelfBcoDateCheckFragment_State"

            fun fromBundle(bundle: Bundle?): State? {
                return bundle?.getSerializable(STATE_KEY) as? State
            }
        }
    }
}