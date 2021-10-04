/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import androidx.annotation.StringRes
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSimpleTextBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class SimpleTextItem(
    @StringRes private val text: Int,
) : BaseBindableItem<ItemSimpleTextBinding>() {

    override fun getLayout() = R.layout.item_simple_text

    override fun bind(viewBinding: ItemSimpleTextBinding, position: Int) {
        viewBinding.text = text
    }

    override fun isSameAs(other: Item<*>): Boolean = other is SimpleTextItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) = other is SimpleTextItem && other.text == text
}
