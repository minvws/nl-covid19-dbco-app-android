/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputType
import androidx.core.widget.doAfterTextChanged
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactNameBinding
import nl.rijksoverheid.dbco.databinding.ItemPhoneInputBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import timber.log.Timber
import java.util.*

class PhoneNumberItem(private var phoneNumber: String?, question: Question?,
                      private val previousAnswer: JsonObject? = null) :
    BaseQuestionItem<ItemPhoneInputBinding>(question) {
    override fun getLayout() = R.layout.item_phone_input
    override fun isRequired() = true

    private var binding: ItemPhoneInputBinding? = null

    override fun bind(viewBinding: ItemPhoneInputBinding, position: Int) {
        binding = viewBinding
        viewBinding.inputField.editText?.apply {
            inputType = InputType.TYPE_CLASS_PHONE
            addTextChangedListener(PhoneNumberFormattingTextWatcher(Locale.getDefault().country))
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
                checkCompleted()
            }
        }

        fillInPreviousAnswer()
        checkCompleted()
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is PhoneNumberItem && other.phoneNumber == phoneNumber

    override fun hasSameContentAs(other: Item<*>) =
        other is PhoneNumberItem && other.phoneNumber == phoneNumber

    override fun isCompleted(): Boolean {
        return !phoneNumber.isNullOrEmpty()
    }

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        phoneNumber?.let {
            answers.put("phoneNumber", it)
        }
        return answers
    }

    private fun fillInPreviousAnswer() {
        if (previousAnswer != null && previousAnswer.containsKey(
                "phoneNumber" )) {
            val previousAnswerValue = previousAnswer["phoneNumber"]?.jsonPrimitive?.content
            Timber.d("Found previous value for \"phoneNumber\" of $previousAnswerValue")
            binding?.let{
                it.inputField.editText?.setText(previousAnswerValue)
                phoneNumber = previousAnswerValue
            }
        }
    }
}