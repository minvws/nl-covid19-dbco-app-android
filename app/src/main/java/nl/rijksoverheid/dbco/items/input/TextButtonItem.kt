/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemTextButtonBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class TextButtonItem(
    private val text: String,
    private val buttonClickListener: () -> Unit
) : BaseBindableItem<ItemTextButtonBinding>() {

    override fun getLayout() = R.layout.item_text_button

    override fun bind(viewBinding: ItemTextButtonBinding, position: Int) {
        viewBinding.button.text = text
        viewBinding.button.setOnClickListener { buttonClickListener() }
    }

    override fun isSameAs(other: Item<*>): Boolean = other is TextButtonItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) = other is TextButtonItem && other.text == text
}
