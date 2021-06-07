/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import androidx.annotation.DimenRes
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemHeaderBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.margin

class HeaderItem(
    private val text: String,
    @DimenRes private val horizontalMargin: Int? = null
) : BaseBindableItem<ItemHeaderBinding>() {

    override fun getLayout() = R.layout.item_header

    override fun bind(viewBinding: ItemHeaderBinding, position: Int) {
        viewBinding.text = text
        viewBinding.content.margin(start = horizontalMargin, end = horizontalMargin)
    }

    override fun isSameAs(other: Item<*>): Boolean = other is HeaderItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) = other is HeaderItem && other.text == text
}
