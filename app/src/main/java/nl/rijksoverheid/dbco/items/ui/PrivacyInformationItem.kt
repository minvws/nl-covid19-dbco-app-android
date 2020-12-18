/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import androidx.core.view.ViewCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemParagraphBinding
import nl.rijksoverheid.dbco.databinding.ItemPrivacyInformationBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.HtmlHelper
import timber.log.Timber

class PrivacyInformationItem(
    private val text: String?
) : BaseBindableItem<ItemPrivacyInformationBinding>() {
    override fun getLayout() = R.layout.item_privacy_information

    override fun bind(viewBinding: ItemPrivacyInformationBinding, position: Int) {
        ViewCompat.enableAccessibleClickableSpanSupport(viewBinding.content)

        Timber.d("Got value $text")
        text?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.text = spannableBuilder
        }
    }

    override fun isClickable() = false
    override fun isSameAs(other: Item<*>): Boolean = other is PrivacyInformationItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is PrivacyInformationItem && other.text == text
}