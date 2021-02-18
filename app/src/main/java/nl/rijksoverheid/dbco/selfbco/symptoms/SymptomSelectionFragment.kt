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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoSymptomsBinding
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ButtonType
import nl.rijksoverheid.dbco.items.input.SymptomItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import nl.rijksoverheid.dbco.selfbco.onboarding.SelfBcoExplanationFragment
import nl.rijksoverheid.dbco.util.hideKeyboard
import timber.log.Timber

class SymptomSelectionFragment : BaseFragment(R.layout.fragment_selfbco_symptoms) {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var selectedItems = 0
    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoSymptomsBinding.bind(view)

        adapter.clear() // Clear adapter in case items were already added, possible on backbutton loop

        val content = Section()
        val nextButton = ButtonItem(getString(R.string.next), {
            findNavController().navigate(
                SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment(
                    SelfBcoConstants.SYMPTOM_CHECK_FLOW
                )
            )
            selfBcoViewModel.setTypeOfFlow(SelfBcoConstants.SYMPTOM_CHECK_FLOW)
        }, type = ButtonType.DARK)

        val noSymptomButton = ButtonItem(getString(R.string.selfbco_symptoms_nosymptoms), {
            findNavController().navigate(
                SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment(
                    SelfBcoConstants.COVID_CHECK_FLOW
                )
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

        SelfBcoConstants.SYMPTOMS.forEach { symptomName ->
            content.add(SymptomItem(symptomName))
        }
        // Button to start with
        content.setFooter(noSymptomButton)

        adapter.add(content)

        binding.content.adapter = adapter
        binding.content.itemAnimator = null // Remove animator here to avoid flashing on clicking symptoms

        adapter.setOnItemClickListener { item, vieww ->
            Timber.d("Item clicked ${item}")
            if (item is SymptomItem) {
                item.selected = !item.selected
                item.setChecked()
                if (item.selected) {
                    selectedItems++
                    selfBcoViewModel.addSymptom(item.text.toString())
                } else {
                    selectedItems--
                    selfBcoViewModel.removeSymptom(item.text.toString())
                }
            }

            if (selfBcoViewModel.getSelectedSymptomsSize() > 0) {
                content.setFooter(nextButton)
            } else {
                content.setFooter(noSymptomButton)
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

    }


}