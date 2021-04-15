/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.timeline

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoTimelineExplanationBinding
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphIconItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem

class TimelineExplanationFragment : BaseFragment(R.layout.fragment_selfbco_timeline_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoTimelineExplanationBinding.bind(view)

        val content = Section(
            listOf(
                HeaderItem(getString(R.string.selfbco_timeline_explanation_header)),
                ParagraphItem(getString(R.string.selfbco_timeline_explanation_summary), clickable = true),
                ParagraphIconItem(getString(R.string.selfbco_timeline_explanation_step1), R.drawable.ic_checkmark_round),
                ParagraphIconItem(getString(R.string.selfbco_timeline_explanation_step2), R.drawable.ic_checkmark_round),
                ParagraphIconItem(getString(R.string.selfbco_timeline_explanation_step3), R.drawable.ic_questionmark_round),
                ParagraphIconItem(getString(R.string.selfbco_timeline_explanation_step4), R.drawable.ic_minus_round),
            )
        )
        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(content)

        binding.content.adapter = adapter

        binding.btnNext.setOnClickListener {
            findNavController().navigate(TimelineExplanationFragmentDirections.toTimelineFragment())
        }

        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}