/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.InputType
import android.text.TextUtils
import androidx.core.widget.doAfterTextChanged
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemEmailInputBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.QuestionnaireItem

class EmailAdressItem(private var emailAddress: String?) :
    BaseBindableItem<ItemEmailInputBinding>(), QuestionnaireItem {
    override fun getLayout() = R.layout.item_email_input
    override fun isRequired() = false
    override fun getItemType() = ItemType.INPUT_EMAIL
    private var isValidEmail: Boolean = false

    override fun bind(viewBinding: ItemEmailInputBinding, position: Int) {
        viewBinding.inputField.editText?.apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(emailAddress)
        }
        viewBinding.inputField.apply {
            this.hint = this.context.getString(R.string.hint_email_address)
        }

        viewBinding.inputField.editText?.doAfterTextChanged {
            if (TextUtils.isEmpty(it) || !android.util.Patterns.EMAIL_ADDRESS.matcher(it)
                    .matches()
            ) {
                viewBinding.inputField.error =
                    viewBinding.inputField.context.getString(R.string.error_valid_email)
                isValidEmail = false
            } else {
                viewBinding.inputField.error = null
                isValidEmail = true
            }

            emailAddress = it.toString()
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is EmailAdressItem && other.emailAddress == emailAddress

    override fun hasSameContentAs(other: Item<*>) =
        other is EmailAdressItem && other.emailAddress == emailAddress

    override fun isCompleted(): Boolean {
        return !emailAddress.isNullOrEmpty() && isValidEmail
    }
}
