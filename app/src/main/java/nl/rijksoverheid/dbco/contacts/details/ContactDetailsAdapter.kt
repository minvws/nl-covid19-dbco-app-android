/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import com.jay.widget.StickyHeaders
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader

class ContactDetailsAdapter : GroupAdapter<GroupieViewHolder>(), StickyHeaders {

    override fun isStickyHeader(position: Int): Boolean {
        val item = getItem(position)
        return item is QuestionnaireSectionHeader
    }
}