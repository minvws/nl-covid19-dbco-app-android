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
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemEmailInputBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive

class EmailAddressItem(
    private var emailAddress: String?,
    question: Question?,
    private val changeListener: (String) -> Unit
) :
    BaseQuestionItem<ItemEmailInputBinding>(question) {
    override fun getLayout() = R.layout.item_email_input
    private var isValidEmail: Boolean = false
    private var binding: ItemEmailInputBinding? = null

    override fun bind(viewBinding: ItemEmailInputBinding, position: Int) {
        binding = viewBinding
        viewBinding.inputField.editText?.apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(emailAddress)
        }
        viewBinding.inputField.apply {
            this.hint = this.context.getString(R.string.hint_email_address)
        }

        viewBinding.inputField.editText?.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                checkCompleted(viewBinding)
            }
        }

        viewBinding.inputField.editText?.doAfterTextChanged {
            emailAddress = it.toString()
            changeListener.invoke(it.toString())
        }

        checkCompleted(viewBinding)
    }

    private fun checkCompleted(viewBinding: ItemEmailInputBinding) {
        val input = viewBinding.inputField.editText?.text.toString()
        if (!TextUtils.isEmpty(input)) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input)
                    .matches()
            ) {
                viewBinding.inputField.error =
                    viewBinding.inputField.context.getString(R.string.error_valid_email)
                isValidEmail = false
            } else {
                viewBinding.inputField.error = null
                isValidEmail = true
                viewBinding.inputField.editText?.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_valid_small,
                    0
                )
                viewBinding.inputField.setEndIconActivated(true)
                changeListener.invoke(input)
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is EmailAddressItem && other.emailAddress == emailAddress

    override fun hasSameContentAs(other: Item<*>) =
        other is EmailAddressItem && other.emailAddress == emailAddress

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        emailAddress?.let {
            answers.put("email", it.toJsonPrimitive())
        }
        return answers
    }
}
