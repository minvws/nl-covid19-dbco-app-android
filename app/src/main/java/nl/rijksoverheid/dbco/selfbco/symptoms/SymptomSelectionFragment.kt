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
import nl.rijksoverheid.dbco.items.input.SymptomItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
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

            if (selectedItems > 0) {
                binding.btnNext.apply {
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.purple)
                    setTextColor(context.getColor(R.color.white))
                    text = getText(R.string.next)
                }
            } else {
                binding.btnNext.apply {
                    backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.gray_lighter)
                    setTextColor(context.getColor(R.color.purple))
                    text = getText(R.string.selfbco_symptoms_nosymptoms)
                }
            }
        }
        binding.content.adapter = adapter

        val content = Section()
        // Add header and summary
        content.addAll(
            listOf(
                HeaderItem(R.string.selfbco_symptom_header),
                ParagraphItem(getString(R.string.selfbco_symptom_summary))
            )
        )

        SYMPTOMS.forEach { symptomName ->
            content.add(SymptomItem(symptomName))
        }
        adapter.add(content)

        binding.btnNext.setOnClickListener {
            if (selectedItems > 0) {
                // Go to date check
                findNavController().navigate(
                    SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment(
                        SelfBcoDateCheckFragment.SYMPTOM_CHECK_FLOW
                    )
                )
            } else {
                // go to
                findNavController().navigate(
                    SymptomSelectionFragmentDirections.toSelfBcoDateCheckFragment(
                        SelfBcoDateCheckFragment.COVID_CHECK_FLOW
                    )
                )
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


    }

    companion object {
        val SYMPTOMS = listOf(
            "Neusverkoudheid",
            "Schorre stem",
            "Keelpijn",
            "(licht) hoesten",
            "Kortademigheid/benauwdheid",
            "Pijn bij de ademhaling",
            "Koorts (= boven 38 graden Celsius)",
            "Koude rillingen",
            "Verlies van of verminderde reuk",
            "Verlies van of verminderde smaak",
            "Algehele malaise",
            "Vermoeidheid",
            "Hoofdpijn",
            "Spierpijn",
            "Pijn achter de ogen",
            "Algehele pijnklachten",
            "Duizeligheid",
            "Prikkelbaarheid/verwardheid",
            "Verlies van eetlust",
            "Misselijkheid",
            "Overgeven",
            "Diarree",
            "Buikpijn",
            "Rode prikkende ogen (oogontsteking)",
            "Huidafwijkingen"
        )

    }


}