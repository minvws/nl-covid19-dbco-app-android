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
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemPhoneInputBinding
import nl.rijksoverheid.dbco.questionnary.data.entity.Question
import java.util.*

class PhoneNumberItem(private var phoneNumber: String?, question: Question?) :
    BaseQuestionItem<ItemPhoneInputBinding>(question) {
    override fun getLayout() = R.layout.item_phone_input
    override fun isRequired() = true

    override fun bind(viewBinding: ItemPhoneInputBinding, position: Int) {
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
}