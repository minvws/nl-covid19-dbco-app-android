/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestionnaireSectionBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class QuestionnaireSection : BaseBindableItem<ItemQuestionnaireSectionBinding>(), ExpandableItem {
    private lateinit var expandableGroup: ExpandableGroup
    override fun bind(viewBinding: ItemQuestionnaireSectionBinding, position: Int) {
        viewBinding.root.setOnClickListener {
            expandableGroup.onToggleExpanded()
        }
    }

    override fun getLayout() = R.layout.item_questionnaire_section

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }

}