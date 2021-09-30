/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.ui

import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import androidx.annotation.DimenRes
import androidx.core.view.ViewCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemParagraphBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper
import nl.rijksoverheid.dbco.util.margin

class ParagraphItem(
    private val text: String?,
    private val clickable: Boolean = false,
    @DimenRes private val horizontalMargin: Int? = null
) : BaseBindableItem<ItemParagraphBinding>() {

    override fun getLayout() = R.layout.item_paragraph

    override fun bind(viewBinding: ItemParagraphBinding, position: Int) {
        viewBinding.content.margin(start = horizontalMargin, end = horizontalMargin)
        text?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            val spans = spannableBuilder.getSpans(0, spannableBuilder.length, ClickableSpan::class.java)
            if (spans.isNotEmpty()) {
                ViewCompat.enableAccessibleClickableSpanSupport(viewBinding.content)
                viewBinding.content.linksClickable = true
                viewBinding.content.movementMethod = LinkMovementMethod.getInstance();
            }
            viewBinding.text = spannableBuilder
        }
    }

    override fun isClickable() = clickable

    override fun isSameAs(other: Item<*>): Boolean = other is ParagraphItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) = other is ParagraphItem && other.text == text
}
