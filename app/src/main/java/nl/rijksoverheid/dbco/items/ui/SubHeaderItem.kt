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
import nl.rijksoverheid.dbco.databinding.ItemSubHeaderBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem


class SubHeaderItem(private val text: String) : BaseBindableItem<ItemSubHeaderBinding>() {
    override fun getLayout() = R.layout.item_sub_header

    override fun bind(viewBinding: ItemSubHeaderBinding, position: Int) {
        viewBinding.text = text
    }

    override fun isSameAs(other: Item<*>): Boolean = other is SubHeaderItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is SubHeaderItem && other.text == text
}
