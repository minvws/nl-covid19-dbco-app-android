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
import nl.rijksoverheid.dbco.databinding.ItemSubHeaderBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper
import nl.rijksoverheid.dbco.util.margin

class SubHeaderItem(
    private val text: String,
    @DimenRes private val horizontalMargin: Int? = null
) : BaseBindableItem<ItemSubHeaderBinding>() {

    override fun getLayout() = R.layout.item_sub_header

    override fun bind(viewBinding: ItemSubHeaderBinding, position: Int) {
        val context = viewBinding.root.context
        val spannableBuilder = HtmlHelper.buildSpannableFromHtml(text, context)
        viewBinding.content.text = spannableBuilder
        viewBinding.content.margin(start = horizontalMargin, end = horizontalMargin)
    }

    override fun isSameAs(other: Item<*>): Boolean = other is SubHeaderItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) = other is SubHeaderItem && other.text == text
}
