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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.config.Symptom
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoSymptomsBinding
import nl.rijksoverheid.dbco.items.VerticalSpaceItemDecoration
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ButtonType
import nl.rijksoverheid.dbco.items.input.SymptomItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.VerticalSpaceItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import java.io.Serializable

class SymptomSelectionFragment : BaseFragment(R.layout.fragment_selfbco_symptoms) {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoSymptomsBinding.bind(view)

        val content = Section()

        val nextButton = ButtonItem(getString(R.string.next), {
            onSymptomsSelected(getSelectedSymptoms())
        }, type = ButtonType.DARK)

        val noSymptomButton = ButtonItem(getString(R.string.selfbco_symptoms_nosymptoms), {
            onNoSymptomsSelected()
        }, type = ButtonType.LIGHT)

        val showMoreButton = ButtonItem(getString(R.string.selfbco_symptoms_show_more), { button ->
            onShowMoreSymptoms(content, button, nextButton, noSymptomButton)
        }, type = ButtonType.BORDERLESS)

        initAdapter(
            content = content,
            showMoreButton = showMoreButton,
            nextButton = nextButton,
            noSymptomButton = noSymptomButton
        )
        initToolbar(binding = binding)
        initHeader(content = content)
        initSymptoms(
            content = content,
            symptoms = State.fromBundle(savedInstanceState)?.symptoms ?: getSymptomsToShow()
        )

        binding.content.itemAnimator = null
        binding.content.addItemDecoration(
            VerticalSpaceItemDecoration(
                requireContext().resources.getDimensionPixelSize(
                    R.dimen.symptom_list_divider_height
                )
            )
        )
        binding.content.adapter = adapter

        updateFooter(
            content = content,
            showMoreButton = showMoreButton,
            nextButton = nextButton,
            noSymptomButton = noSymptomButton,
            showMoreButtonVisible = !allSymptomsShown()
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val state = getState()
        if (state.symptoms.isNotEmpty()) {
            state.addToBundle(outState)
        }
        super.onSaveInstanceState(outState)
    }

    private fun initAdapter(
        content: Section,
        showMoreButton: ButtonItem,
        nextButton: ButtonItem,
        noSymptomButton: ButtonItem,
    ) {
        with(adapter) {
            clear()
            setOnItemClickListener { item, _ ->
                if (item is SymptomItem) {
                    item.selected = !item.selected
                    item.setChecked()
                }
                updateFooter(
                    content = content,
                    showMoreButton = showMoreButton,
                    nextButton = nextButton,
                    noSymptomButton = noSymptomButton,
                    showMoreButtonVisible = !allSymptomsShown()
                )
            }
            add(content)
        }
    }

    private fun initToolbar(binding: FragmentSelfbcoSymptomsBinding) {
        binding.toolbar.backButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initHeader(content: Section) {
        content.addAll(
            listOf(
                HeaderItem(getString(R.string.selfbco_symptom_header)),
                VerticalSpaceItem(R.dimen.list_spacing),
                ParagraphItem(getString(R.string.selfbco_symptom_summary)),
                VerticalSpaceItem(R.dimen.activity_vertical_margin)
            )
        )
    }

    private fun initSymptoms(content: Section, symptoms: List<State.SymptomState>) {
        symptoms.map(::mapToSymptomItem).forEach { symptom ->
            content.add(symptom)
            content.add(VerticalSpaceItem(R.dimen.list_spacing))
        }
    }

    private fun mapToSymptomItem(symptom: State.SymptomState): SymptomItem {
        return SymptomItem(
            label = symptom.label,
            value = symptom.value,
            selected = symptom.isSelected
        )
    }

    private fun onShowMoreSymptoms(
        content: Section,
        showMoreButton: ButtonItem,
        nextButton: ButtonItem,
        noSymptomButton: ButtonItem
    ) {
        val allSymptoms = selfBcoViewModel.getSymptoms()
        addSymptoms(
            symptoms = allSymptoms.subList(INITIAL_SYMPTOM_SIZE, allSymptoms.size),
            content = content
        )
        updateFooter(
            content = content,
            showMoreButton = showMoreButton,
            nextButton = nextButton,
            noSymptomButton = noSymptomButton,
            showMoreButtonVisible = false
        )
    }

    /**
     * User has entered symptoms, use symptom flow
     * @param selectedSymptoms the entered symptoms
     */
    private fun onSymptomsSelected(selectedSymptoms: List<Symptom>) {
        with(selfBcoViewModel) {
            setTypeOfFlow(SelfBcoConstants.SYMPTOM_CHECK_FLOW)
            setSelectedSymptoms(selectedSymptoms)
        }
        findNavController().navigate(
            SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment(
                state = SelfBcoDateCheckState.createSymptomState(requireContext())
            )
        )
    }

    /**
     * User has entered no symptoms, use testing flow
     */
    private fun onNoSymptomsSelected() {
        with(selfBcoViewModel) {
            setTypeOfFlow(SelfBcoConstants.COVID_CHECK_FLOW)
            setSelectedSymptoms(emptyList())
        }
        findNavController().navigate(
            SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment(
                state = SelfBcoDateCheckState.createTestState(requireContext())
            )
        )
    }

    private fun getSymptomsToShow(): List<State.SymptomState> {
        val selectedSymptoms = selfBcoViewModel.getSelectedSymptoms()
        val symptoms = selfBcoViewModel.getSymptoms()

        var result = symptoms.subList(0, INITIAL_SYMPTOM_SIZE) // start with subset

        for (selected in selectedSymptoms) {
            if (!result.any { it.value == selected }) {
                result = symptoms // if index has selected symptom outside subset, show all symptoms
            }
        }

        return result.map { symptom ->
            State.SymptomState(
                label = symptom.label,
                value = symptom.value,
                isSelected = selectedSymptoms.contains(symptom.value)
            )
        }
    }

    /**
     * @return whether all possible symptoms are currently shown
     */
    private fun allSymptomsShown(): Boolean {
        return getState().symptoms.size == selfBcoViewModel.getSymptoms().size
    }

    private fun addSymptoms(symptoms: List<Symptom>, content: Section) {
        symptoms.map { symptom ->
            SymptomItem(
                label = symptom.label,
                value = symptom.value,
                selected = false
            )
        }.forEach { item ->
            content.add(item)
            content.add(VerticalSpaceItem(R.dimen.list_spacing))
        }
    }

    /**
     * @return all currently selected symptoms
     */
    private fun getSelectedSymptoms(): List<Symptom> {
        return getState()
            .symptoms
            .filter { item -> item.isSelected }
            .map { item -> Symptom(item.label, item.value) }
    }

    private fun updateFooter(
        content: Section,
        showMoreButton: ButtonItem,
        nextButton: ButtonItem,
        noSymptomButton: ButtonItem,
        showMoreButtonVisible: Boolean
    ) {
        Section().apply {
            if (showMoreButtonVisible) {
                add(showMoreButton)
            }
            if (getSelectedSymptoms().isNotEmpty()) {
                add(nextButton)
            } else {
                add(noSymptomButton)
            }
        }.also { footer -> content.setFooter(footer) }
    }

    private fun getState(): State {
        val symptoms = mutableListOf<SymptomItem>()
        for (groupIndex: Int in 0 until adapter.itemCount) {
            val item = adapter.getItem(groupIndex)
            if (item is SymptomItem) {
                symptoms.add(item)
            }
        }
        return State(
            symptoms = symptoms.map { symptom ->
                State.SymptomState(
                    label = symptom.label.toString(),
                    value = symptom.value,
                    isSelected = symptom.selected
                )
            }
        )
    }

    private data class State(
        val symptoms: List<SymptomState>
    ) : Serializable {

        data class SymptomState(
            val label: String,
            val value: String,
            val isSelected: Boolean
        ) : Serializable

        fun addToBundle(bundle: Bundle) {
            bundle.putSerializable(STATE_KEY, this)
        }

        companion object {
            private const val STATE_KEY = "SymptomSelectionFragment_State"

            fun fromBundle(bundle: Bundle?): State? {
                return bundle?.getSerializable(STATE_KEY) as? State
            }
        }
    }

    companion object {

        private const val INITIAL_SYMPTOM_SIZE = 14
    }
}