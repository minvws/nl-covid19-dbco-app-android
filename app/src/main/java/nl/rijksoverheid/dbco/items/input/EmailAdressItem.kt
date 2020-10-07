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
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSingleInputBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType

class EmailAdressItem(private val emailAddress: String?) :
    BaseBindableItem<ItemSingleInputBinding>() {
    override fun getLayout() = R.layout.item_single_input
    override fun isRequired() = true
    override val itemType = ItemType.INPUT_EMAIL

    override fun bind(viewBinding: ItemSingleInputBinding, position: Int) {
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
            } else {
                viewBinding.inputField.error = null
            }
        }
    }
}
