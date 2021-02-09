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
import nl.rijksoverheid.dbco.databinding.ItemDuoHeaderBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem


class DuoHeaderItem(private val header: String, private val subtitle: String) :
    BaseBindableItem<ItemDuoHeaderBinding>() {
    override fun getLayout() = R.layout.item_duo_header

    override fun bind(viewBinding: ItemDuoHeaderBinding, position: Int) {
        viewBinding.header = header
        viewBinding.summary = subtitle
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is DuoHeaderItem && other.header == header

    override fun hasSameContentAs(other: Item<*>) =
        other is DuoHeaderItem && other.header == header
}
