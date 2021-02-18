/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemButtonBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import timber.log.Timber

class ButtonItem(
    private val text: String,
    buttonClickListener: () -> Unit,
    private val enabled: Boolean = true,
    private val type: ButtonType = ButtonType.LIGHT,
    private val wide: Boolean = false
) : BaseBindableItem<ItemButtonBinding>() {
    data class ViewState(
        val text: String,
        val enabled: Boolean,
        val click: () -> Unit,
        val wide: Boolean
    )

    private val viewState =
        ViewState(
            text,
            enabled,
            buttonClickListener,
            wide
        )

    override fun getLayout() = R.layout.item_button


    override fun bind(viewBinding: ItemButtonBinding, position: Int) {
        viewBinding.viewState = viewState
        if (type == ButtonType.DARK) {
            viewBinding.button.apply {
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.purple)
                setTextColor(context.getColor(R.color.white))
                setButtonWide(this)
            }
        } else if (type == ButtonType.LIGHT) {
            viewBinding.button.apply {
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_lighter)
                setTextColor(context.getColor(R.color.purple))
                setButtonWide(this)
            }
        }
    }

    /**
     * Handling dynamic margin - used to deal with certain screens using padding on their RV or not
     * To do: Move to BindingAdapter, wouldn't trigger for some reason
     */
    private fun setButtonWide(button : Button){
        val layoutParams = button.layoutParams as ViewGroup.MarginLayoutParams
        if (!wide) {
            layoutParams.marginStart =
                button.context.resources.getDimension(R.dimen.activity_horizontal_margin).toInt()
            layoutParams.marginEnd =
                button.context.resources.getDimension(R.dimen.activity_horizontal_margin).toInt()
        } else {
            layoutParams.marginStart =
                button.context.resources.getDimension(R.dimen.none).toInt()
            layoutParams.marginEnd =
                button.context.resources.getDimension(R.dimen.none).toInt()
        }
        button.layoutParams = layoutParams
    }

    override fun isSameAs(other: Item<*>): Boolean = other is ButtonItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is ButtonItem && other.text == text &&
            other.enabled == enabled
}

@Keep
enum class ButtonType { LIGHT, DARK }
