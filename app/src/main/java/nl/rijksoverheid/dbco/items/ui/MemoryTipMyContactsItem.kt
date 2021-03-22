/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.databinding.ItemMemorytipMycontactsGrayBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper
import org.joda.time.LocalDate


/**
 * Simple class showing a static memory tip on My Contacts
 */
class MemoryTipMyContactsItem(val date: String?) :
    BaseBindableItem<ItemMemorytipMycontactsGrayBinding>() {

    override fun bind(viewBinding: ItemMemorytipMycontactsGrayBinding, position: Int) {

        val onset = LocalDate.parse(date, DateFormats.dateInputData) ?: LocalDate.now()
        val context = viewBinding.root.context
        val spannableBuilder = HtmlHelper.buildSpannableFromHtml(
            String.format(
                context.getString(R.string.mycontacts_memory_tip_summary),
                onset.toString(DateFormats.selfBcoDateOnly)
            ), context
        )

        viewBinding.memorySummary.text = spannableBuilder
    }

    override fun getLayout(): Int = R.layout.item_memorytip_mycontacts_gray
    override fun isClickable(): Boolean = true
    override fun isLongClickable() = true
}