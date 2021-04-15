/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.onboarding

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPermissionExplanationBinding
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphIconItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem

class SelfBcoPermissionExplanationFragment :
    BaseFragment(R.layout.fragment_selfbco_permission_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSelfbcoPermissionExplanationBinding.bind(view)

        val content = Section(
            listOf(
                HeaderItem(getString(R.string.selfbco_permission_explanation_header)),
                ParagraphItem(getString(R.string.selfbco_permission_extra_summary), clickable = true),
                ParagraphIconItem(getString(R.string.selfbco_permission_extra_item1)),
                ParagraphIconItem(getString(R.string.selfbco_permission_extra_item2)),
                ParagraphIconItem(getString(R.string.selfbco_permission_extra_item3))
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