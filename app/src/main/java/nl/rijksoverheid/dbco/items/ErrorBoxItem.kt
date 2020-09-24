/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.dbco.items

import androidx.annotation.StringRes
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemErrorBoxBinding

class ErrorBoxItem(@StringRes text: Int, @StringRes actionLabel: Int, action: () -> Unit = {}) :
    BaseBindableItem<ItemErrorBoxBinding>() {

    data class ViewState(
        val text: Int,
        val actionLabel: Int,
        val action: () -> Unit
    )

    val viewState = ViewState(text, actionLabel, action)

    override fun getLayout() = R.layout.item_error_box

    override fun bind(viewBinding: ItemErrorBoxBinding, position: Int) {
        viewBinding.viewState = viewState
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is ErrorBoxItem && other.viewState.text == viewState.text

    override fun hasSameContentAs(other: Item<*>) =
        other is ErrorBoxItem && other.viewState.text == viewState.text
}
