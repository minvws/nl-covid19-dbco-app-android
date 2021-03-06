/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import android.text.method.LinkMovementMethod
import androidx.core.view.ViewCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemFooterBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper

class FooterItem(
        private val text: String?,
        private val clickable: Boolean = false
) : BaseBindableItem<ItemFooterBinding>() {
    override fun getLayout() = R.layout.item_footer

    override fun bind(viewBinding: ItemFooterBinding, position: Int) {
        ViewCompat.enableAccessibleClickableSpanSupport(viewBinding.content)
        viewBinding.content.linksClickable = true
        viewBinding.content.movementMethod = LinkMovementMethod.getInstance();

        text?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.text = spannableBuilder
        }
    }

    override fun isClickable() = clickable
    override fun isSameAs(other: Item<*>): Boolean = other is FooterItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is FooterItem && other.text == text
}
