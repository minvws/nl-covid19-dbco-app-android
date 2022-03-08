/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemLinkBinding
import nl.rijksoverheid.dbco.databinding.ItemSubHeaderBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper

class LinkItem(
    private val text: String,
    private val clickListener: () -> Unit
) : BaseBindableItem<ItemLinkBinding>() {

    override fun getLayout() = R.layout.item_link

    override fun bind(viewBinding: ItemLinkBinding, position: Int) {
        viewBinding.content.text = text
        viewBinding.content.setOnClickListener {
            clickListener()
        }
    }

    override fun isSameAs(other: Item<*>): Boolean = other is LinkItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) = other is LinkItem && other.text == text
}
