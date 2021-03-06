/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemHeaderIconBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper

class HeaderIconItem(
    private val text: String?,
    @DrawableRes private val icon: Int = R.drawable.ic_privacy_lock
) : BaseBindableItem<ItemHeaderIconBinding>() {

    override fun getLayout() = R.layout.item_header_icon

    override fun bind(viewBinding: ItemHeaderIconBinding, position: Int) {
        ViewCompat.enableAccessibleClickableSpanSupport(viewBinding.content)

        text?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.text = spannableBuilder
        }
        viewBinding.icon.setImageResource(icon)
    }

    override fun isClickable() = false

    override fun isSameAs(other: Item<*>): Boolean = other is HeaderIconItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) = other is HeaderIconItem && other.text == text
}