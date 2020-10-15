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
import nl.rijksoverheid.dbco.databinding.ItemTinyHeaderBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem


class TinyHeaderItem(@StringRes private val text: Int) : BaseBindableItem<ItemTinyHeaderBinding>() {
    override fun getLayout() = R.layout.item_tiny_header

    override fun bind(viewBinding: ItemTinyHeaderBinding, position: Int) {
        viewBinding.text = text
    }

    override fun isSameAs(other: Item<*>): Boolean = other is TinyHeaderItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is TinyHeaderItem && other.text == text
}
