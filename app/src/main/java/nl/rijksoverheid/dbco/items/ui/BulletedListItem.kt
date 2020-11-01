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
import nl.rijksoverheid.dbco.databinding.ItemParagraphBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper


class BulletedListItem(
    @StringRes private val text: Int
) : BaseBindableItem<ItemParagraphBinding>() {
    override fun getLayout() = R.layout.item_paragraph

    override fun bind(viewBinding: ItemParagraphBinding, position: Int) {
        val context = viewBinding.root.context
        val html = context.getString(text)

        val spannableBuilder = HtmlHelper.buildSpannableFromHtml(html, context)
        viewBinding.text = spannableBuilder
    }

    override fun isSameAs(other: Item<*>): Boolean = other is BulletedListItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is BulletedListItem && other.text == text
}
