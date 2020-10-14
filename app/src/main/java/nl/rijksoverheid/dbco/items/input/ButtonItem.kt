/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemButtonBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class ButtonItem(
    @StringRes private val text: Int,
    buttonClickListener: () -> Unit,
    private val enabled: Boolean = true,
    private val type: ButtonType = ButtonType.REGULAR
) : BaseBindableItem<ItemButtonBinding>() {
    data class ViewState(
        @StringRes val text: Int,
        val enabled: Boolean,
        val click: () -> Unit
    )

    private val viewState =
        ViewState(
            text,
            enabled,
            buttonClickListener
        )

    override fun getLayout() = R.layout.item_button


    override fun bind(viewBinding: ItemButtonBinding, position: Int) {
        viewBinding.viewState = viewState
        if (type == ButtonType.LIGHT) {
            viewBinding.button.apply {
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_lighter)
                setTextColor(context.getColor(R.color.dark_blue))
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean = other is ButtonItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is ButtonItem && other.text == text &&
            other.enabled == enabled
}

@Keep
enum class ButtonType { REGULAR, LIGHT }
