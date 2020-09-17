/*
 *   Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *    SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.about.faq

import androidx.annotation.StringRes
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemFaqBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class FAQItem(val id: FAQItemId, @StringRes private val text: Int) :
    BaseBindableItem<ItemFaqBinding>() {
    override fun getLayout() = R.layout.item_faq

    override fun bind(viewBinding: ItemFaqBinding, position: Int) {
        viewBinding.text = text
    }

    override fun isClickable() = true
    override fun isSameAs(other: Item<*>): Boolean = other is FAQItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is FAQItem && other.text == text
}