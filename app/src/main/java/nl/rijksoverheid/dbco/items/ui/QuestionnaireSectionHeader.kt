/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import androidx.annotation.StringRes
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestionnaireSectionBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class QuestionnaireSectionHeader(
    @StringRes val sectionTitle: Int,
    @StringRes val sectionSubtext: Int,
    private var sectionNumber: Int = 1
) :
    BaseBindableItem<ItemQuestionnaireSectionBinding>(), ExpandableItem {
    private lateinit var expandableGroup: ExpandableGroup
    private lateinit var binding: ItemQuestionnaireSectionBinding
    override fun bind(viewBinding: ItemQuestionnaireSectionBinding, position: Int) {
        this.binding = viewBinding
        viewBinding.root.setOnClickListener {
            expandableGroup.onToggleExpanded()
            viewBinding.sectionChevron.setImageResource(getSectionChevron())
        }

        viewBinding.sectionHeader.setText(sectionTitle)
        viewBinding.sectionSubtext.setText(sectionSubtext)
        viewBinding.sectionChevron.setImageResource(getSectionChevron())
        viewBinding.sectionStatusIcon.setImageResource(getSectionIcon())
    }

    override fun getLayout() = R.layout.item_questionnaire_section

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }

    private fun getSectionChevron() =
        if (expandableGroup.isExpanded) R.drawable.ic_chevron_up_round else R.drawable.ic_chevron_down_round

    private fun getSectionIcon() =
        when (sectionNumber) {
            1 -> R.drawable.ic_section_one
            2 -> R.drawable.ic_section_two
            3 -> R.drawable.ic_section_three
            else -> R.drawable.ic_valid
        }


    fun setSectionDone(sectionDone: Boolean) {
        if (sectionDone) {
            binding.sectionStatusIcon.setImageResource(R.drawable.ic_valid)
        } else {
            binding.sectionStatusIcon.setImageResource(getSectionIcon())
        }
    }

}