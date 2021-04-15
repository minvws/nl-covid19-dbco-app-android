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
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoSymptomsExplanationBinding
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem

class SelfBcoSymptomsExplanationFragment :
    BaseFragment(R.layout.fragment_selfbco_symptoms_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSelfbcoSymptomsExplanationBinding.bind(view)

        val content = Section(
            listOf(
                HeaderItem(getString(R.string.selfbco_symptoms_explanation_title)),
                SubHeaderItem(getString(R.string.selfbco_symptoms_explanation_header_1)),
                ParagraphItem(getString(R.string.selfbco_symptoms_explanation_description_1)),
                SubHeaderItem(getString(R.string.selfbco_symptoms_explanation_header_2)),
                ParagraphItem(getString(R.string.selfbco_symptoms_explanation_description_2)),
            )
        )
        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(content)

        binding.content.adapter = adapter

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
