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
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemPhoneInputBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.setCompleted
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import java.util.*

class PhoneNumberItem(
    private var phoneNumber: String?,
    question: Question?,
    private val changeListener: (String) -> Unit
) :
    BaseQuestionItem<ItemPhoneInputBinding>(question) {
    override fun getLayout() = R.layout.item_phone_input
    private var binding: ItemPhoneInputBinding? = null

    override fun bind(viewBinding: ItemPhoneInputBinding, position: Int) {
        binding = viewBinding
        viewBinding.inputField.editText?.apply {
            inputType = InputType.TYPE_CLASS_PHONE
            setText(phoneNumber)
        }

        viewBinding.inputField.apply {
            this.hint = this.context.getString(R.string.hint_phone_number)
        }

        viewBinding.inputField.editText?.doAfterTextChanged {
            phoneNumber = it.toString()

        }

        viewBinding.inputField.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkCompleted(viewBinding)
                phoneNumber?.let {
                    changeListener.invoke(it)
                }

            }
        }

        checkCompleted(viewBinding)
    }

    private fun checkCompleted(viewBinding: ItemPhoneInputBinding) {
        val input = viewBinding.inputField.editText?.text.toString()
            input.apply {
                // Replace whitespace and parentheses as these shouldn't count towards the character limit
                replace(Regex("[\\s)(]"), "")
            }
        if (!TextUtils.isEmpty(input)) {
            if (!Constants.PHONE_VALIDATION_MATCHER.matcher(input).matches()) {
                viewBinding.inputField.error =
                    viewBinding.inputField.context.getString(R.string.error_valid_phone)
                viewBinding.inputField.setCompleted(false)
            } else {
                // Special case for numbers of length 11 to 13
                if (input.length in 11..13) {
                    // If its not a number starting with our valid prefixes, don't allow it
                    if (!Constants.VALID_PHONENUMER_PREFIXES.any { input.startsWith(it) }) {
                        viewBinding.inputField.error = viewBinding.inputField.context.getString(R.string.error_valid_phone)
                        viewBinding.inputField.setCompleted(false)
                    } else {
                        viewBinding.inputField.error = null
                        viewBinding.inputField.setCompleted(true)
                        changeListener.invoke(input)
                    }
                    // Branch for length = 10, all others fall outside of the matcher
                } else {
                    viewBinding.inputField.error = null
                    viewBinding.inputField.setCompleted(true)
                    changeListener.invoke(input)
                }
            }
        } else {
            viewBinding.inputField.setCompleted(false)
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is PhoneNumberItem && other.phoneNumber == phoneNumber

    override fun hasSameContentAs(other: Item<*>) =
        other is PhoneNumberItem && other.phoneNumber == phoneNumber

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        phoneNumber?.let {
            answers.put("phoneNumber", it.toJsonPrimitive())
        }
        return answers
    }
}