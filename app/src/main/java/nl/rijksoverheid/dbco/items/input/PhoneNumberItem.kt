/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.InputType
import androidx.core.widget.doAfterTextChanged
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemPhoneInputBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import java.util.*

class PhoneNumberItem(private var phoneNumber: String?, question: Question?, private val changeListener: (String) -> Unit) :
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
            changeListener.invoke(it.toString())
        }

        viewBinding.inputField.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkCompleted()
            }
        }

        checkCompleted()
    }

    private fun checkCompleted() {
        // TODO check phone
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