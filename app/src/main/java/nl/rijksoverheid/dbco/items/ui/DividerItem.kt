/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemDividerBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.margin

class DividerItem(
    @DimenRes private val height: Int,
    @DimenRes private val verticalMargin: Int? = null,
    @ColorRes private val color: Int? = null,
) : BaseBindableItem<ItemDividerBinding>() {

    override fun getLayout() = R.layout.item_divider

    override fun bind(viewBinding: ItemDividerBinding, position: Int) {
        with(viewBinding.divider) {
            layoutParams.height = resources.getDimensionPixelSize(this@DividerItem.height)
            margin(top = verticalMargin, bottom = verticalMargin)
            color?.let { setBackgroundColor(ContextCompat.getColor(context, it)) }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is DividerItem && other.height == height

    override fun hasSameContentAs(other: Item<*>) =
        other is DividerItem && other.height == height
}