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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoSymptomsBinding
import nl.rijksoverheid.dbco.items.VerticalSpaceItemDecoration
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ButtonType
import nl.rijksoverheid.dbco.items.input.SymptomItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import timber.log.Timber

class SymptomSelectionFragment : BaseFragment(R.layout.fragment_selfbco_symptoms) {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoSymptomsBinding.bind(view)

        adapter.clear() // Clear adapter in case items were already added, possible on back button loop

        val content = Section()
        val nextButton = ButtonItem(getString(R.string.next), {
            findNavController().navigate(
                SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment()
            )
            selfBcoViewModel.setTypeOfFlow(SelfBcoConstants.SYMPTOM_CHECK_FLOW)
        }, type = ButtonType.DARK)

        val noSymptomButton = ButtonItem(getString(R.string.selfbco_symptoms_nosymptoms), {
            findNavController().navigate(
                SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment()
            )
            selfBcoViewModel.setTypeOfFlow(SelfBcoConstants.COVID_CHECK_FLOW)
        }, type = ButtonType.LIGHT)

        // Add header and summary
        content.addAll(
            listOf(
                HeaderItem(R.string.selfbco_symptom_header),
                ParagraphItem(getString(R.string.selfbco_symptom_summary))
            )
        )

        val selectedSymptoms = selfBcoViewModel.getSelectedSymptoms()
        val symptoms = selfBcoViewModel.getSymptoms()
        symptoms.forEach { symptom ->
            content.add(
                SymptomItem(
                    label = symptom.label,
                    value = symptom.value,
                    selected = selectedSymptoms.contains(symptom.value)
                )
            )
        }
        // Button to start with
        content.setFooter(noSymptomButton)

        adapter.add(content)

        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            VerticalSpaceItemDecoration(
                requireContext().resources.getDimensionPixelSize(R.dimen.symptom_list_divider_height)
            )
        )
        binding.content.itemAnimator =
            null // Remove animator here to avoid flashing on clicking symptoms

        adapter.setOnItemClickListener { item, _ ->
            Timber.d("Item clicked $item")
            if (item is SymptomItem) {
                item.selected = !item.selected
                item.setChecked()
                if (item.selected) {
                    selfBcoViewModel.addSymptom(item.value)
                } else {
                    selfBcoViewModel.removeSymptom(item.value)
                }
            }
            updateFooter(content, nextButton, noSymptomButton)
        }

        updateFooter(content, nextButton, noSymptomButton)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updateFooter(
        content: Section,
        nextButton: ButtonItem,
        noSymptomButton: ButtonItem
    ) {
        if (selfBcoViewModel.getSelectedSymptomsSize() > 0) {
            content.setFooter(nextButton)
        } else {
            content.setFooter(noSymptomButton)
        }
    }
}