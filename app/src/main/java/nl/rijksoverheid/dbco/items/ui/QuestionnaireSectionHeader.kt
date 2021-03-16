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
import nl.rijksoverheid.dbco.util.accessibilityAnnouncement
import nl.rijksoverheid.dbco.util.delay
import nl.rijksoverheid.dbco.util.setImageResource

class QuestionnaireSectionHeader(
    @StringRes val sectionTitle: Int,
    @StringRes val sectionSubtext: Int,
    var sectionNumber: Int = 1
) : BaseBindableItem<ItemQuestionnaireSectionBinding>(), ExpandableItem {
    private lateinit var expandableGroup: ExpandableGroup
    private var binding: ItemQuestionnaireSectionBinding? = null

    var completed = false
        set(value) {
            field = value
            if (value) {
                enabled = true
            }
        }

    var enabled = false
        set(value) {
            field = value

            if (!value && expandableGroup.isExpanded) {
                expandableGroup.onToggleExpanded()
            }

            updateSectionStatus(binding)
        }

    var blocked = false

    override fun bind(viewBinding: ItemQuestionnaireSectionBinding, position: Int) {
        this.binding = viewBinding
        viewBinding.root.setOnClickListener {
            if (enabled && !blocked) {
                expandableGroup.onToggleExpanded()
                updateChevron()
            }
        }

        viewBinding.sectionHeader.setText(sectionTitle)
        viewBinding.sectionSubtext.setText(sectionSubtext)

        updateChevron()
        updateSectionStatus(viewBinding)
    }

    private fun updateSectionStatus(binding: ItemQuestionnaireSectionBinding?) {
        val icon = getIcon()
        binding?.sectionStatusIcon?.setImageResource(icon.imageId, icon.stringId)
        binding?.sectionStatusIcon?.isEnabled = enabled

        binding?.sectionContainer?.isEnabled = enabled

        if (completed) {
            getAnnouncement()?.let { announcementId ->
                delay(500) {
                    binding?.sectionStatusIcon?.context?.accessibilityAnnouncement(announcementId)
                }
            }
        }
    }

    private fun getAnnouncement(): Int? {
        return when (sectionNumber) {
            1 -> R.string.contact_section_announcement_one
            2 -> R.string.contact_section_announcement_two
            3 -> R.string.contact_section_announcement_three
            else -> null
        }
    }

    fun updateChevron() {
        val chevron = getChevron()
        binding?.sectionChevron?.setImageResource(chevron.imageId, chevron.stringId)
    }

    override fun getLayout() = R.layout.item_questionnaire_section

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }

    private enum class Chevron(val imageId: Int, val stringId: Int) {
        EXPANDED(R.drawable.ic_chevron_up_round, R.string.expanded),
        COLLAPSED(R.drawable.ic_chevron_down_round, R.string.collapsed)
    }

    private fun getChevron(): Chevron {
        return if (expandableGroup.isExpanded) {
            Chevron.EXPANDED
        } else {
            Chevron.COLLAPSED
        }
    }

    private enum class Icon(val imageId: Int, val stringId: Int) {
        ONE(R.drawable.ic_section_one, R.string.contact_section_icon_one),
        TWO(R.drawable.ic_section_two, R.string.contact_section_icon_two),
        THREE(R.drawable.ic_section_three, R.string.contact_section_icon_three),
        VALID(R.drawable.ic_valid, R.string.completed)
    }

    private fun getIcon(): Icon {
        if (completed && enabled) {
            return Icon.VALID
        }
        return when (sectionNumber) {
            1 -> Icon.ONE
            2 -> Icon.TWO
            3 -> Icon.THREE
            else -> Icon.VALID
        }
    }
}