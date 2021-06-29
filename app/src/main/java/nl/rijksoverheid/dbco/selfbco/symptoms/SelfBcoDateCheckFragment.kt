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
import com.google.android.material.datepicker.*
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBinding
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBindingImpl
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.symptoms.SelfBcoDateCheckNavigation.*
import nl.rijksoverheid.dbco.selfbco.symptoms.SelfBcoDateCheckState.DateCheckType
import nl.rijksoverheid.dbco.selfbco.symptoms.SelfBcoDateCheckState.DateCheckType.*
import nl.rijksoverheid.dbco.util.HtmlHelper
import nl.rijksoverheid.dbco.util.hideKeyboard
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import java.io.Serializable
import java.util.*

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

        val argState: SelfBcoDateCheckState = args.state
        val date = State.fromBundle(savedInstanceState)?.date ?: getStoredDate(argState.type)

        initToolbar()
        initContent(argState, date)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        getState()?.addToBundle(outState)
        super.onSaveInstanceState(outState)
    }

    private fun initToolbar() {
        binding.toolbar.backButton.setOnClickListener {
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

        displayDate(date)

        binding.datePickerContainer.setOnClickListener { showDatePicker() }

        binding.btnNext.setOnClickListener {
            val result = save(state)
            handleNavigation(state.nextAction(result, LocalDate.now()))
        }

        binding.btnInfo.setOnClickListener {
            save(state)
            findNavController().navigate(
                SelfBcoDateCheckFragmentDirections.toSelfBcoSymptomsExplanationFragment()
            )
        }

        binding.btnInfo.isVisible = state.showExplanation
    }

    private fun displayDate(date: LocalDate) {
        binding.date.text =
            date.toString(DateFormats.datePickerDate).capitalize(Locale.getDefault())
        binding.year.text = date.toString(DateFormats.datePickerYear)
    }

    private fun showDatePicker() {
        val constrains = CalendarConstraints.Builder()
            .setValidator(
                CompositeDateValidator.allOf(
                    listOf(
                        DateValidatorPointForward.from(
                            DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay().millis
                        ),
                        DateValidatorPointBackward.before(
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    )
                )
            )
            .build()

        MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.selfbco_date_title)
            .setSelection(requireDate().toDateTimeAtStartOfDay(DateTimeZone.UTC).millis)
            .setCalendarConstraints(constrains)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    displayDate(LocalDate(selection))
                }
            }.also { it.show(parentFragmentManager, "DatePicker"); }
    }

    private fun save(state: SelfBcoDateCheckState): LocalDate {
        val date = requireDate()
        saveDate(state.type, date)
        return date
    }

    private fun getStoredDate(dateCheckType: DateCheckType): LocalDate {
        return when (dateCheckType) {
            SYMPTOM_ONSET -> selfBcoViewModel.getDateOfSymptomOnset()
            SYMPTOMS_INCREASED_DATE -> selfBcoViewModel.getDateOfIncreasedSymptoms()
            TEST_DATE -> selfBcoViewModel.getDateOfTest()
            POSITIVE_TEST_DATE -> selfBcoViewModel.getDateOfPositiveTest()
            NEGATIVE_TEST_DATE -> selfBcoViewModel.getDateOfNegativeTest()
        }
    }

    private fun saveDate(dateCheckType: DateCheckType, date: LocalDate) {
        when (dateCheckType) {
            SYMPTOM_ONSET -> selfBcoViewModel.updateDateOfSymptomOnset(date)
            SYMPTOMS_INCREASED_DATE -> selfBcoViewModel.updateDateOfIncreasedSymptoms(date)
            TEST_DATE -> selfBcoViewModel.updateTestDate(date)
            POSITIVE_TEST_DATE -> selfBcoViewModel.updateDateOfPositiveTest(date)
            NEGATIVE_TEST_DATE -> selfBcoViewModel.updateDateOfNegativeTest(date)
        }
    }

    private fun requireDate(): LocalDate = getState()!!.date

    private fun getState(): State? {
        return if (::binding.isInitialized) {
            State(
                date = LocalDate.parse(
                    "${binding.date.text.toString().lowercase()} ${binding.year.text}",
                    DateFormats.datePicker
                )
            )
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
                SelfBcoDateCheckFragmentDirections.toSelfBcoChronicSymptomsWorsenedFragment(
                    date = selfBcoViewModel
                        .getDateOfSymptomOnset()
                        .toString(DateFormats.selfBcoDateCheck)
                )
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