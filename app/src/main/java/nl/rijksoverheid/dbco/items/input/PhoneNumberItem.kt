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
import nl.rijksoverheid.dbco.databinding.ItemPhoneInputBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import java.util.*
import java.util.regex.Pattern

class PhoneNumberItem(
    private var phoneNumber: String?,
    question: Question?,
    private val changeListener: (String) -> Unit
) :
    BaseQuestionItem<ItemPhoneInputBinding>(question) {
    override fun getLayout() = R.layout.item_phone_input
    private var binding: ItemPhoneInputBinding? = null

    private val phoneNumberPattern = Pattern.compile("[+]?[0-9]{10,13}$")

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
            changeListener.invoke(it.toString())
        }

        viewBinding.inputField.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkCompleted(viewBinding)
            }
        }

        checkCompleted(viewBinding)
    }

    private fun checkCompleted(viewBinding: ItemPhoneInputBinding) {
        val input = viewBinding.inputField.editText?.text.toString().replace(" ", "")
        if (!TextUtils.isEmpty(input)) {
            if (!phoneNumberPattern.matcher(input).matches()) {
                viewBinding.inputField.error =
                    viewBinding.inputField.context.getString(R.string.error_valid_phone)
                viewBinding.inputField.editText?.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
            } else {
                viewBinding.inputField.error = null
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