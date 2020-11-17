/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.os.Handler
import android.text.InputType
import android.text.TextUtils
import androidx.core.widget.doAfterTextChanged
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemEmailInputBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question

class EmailAddressItem(private var emailAddress: String?, question: Question?, private val editedistener: (String) -> Unit) :
    BaseQuestionItem<ItemEmailInputBinding>(question) {
    override fun getLayout() = R.layout.item_email_input
    override fun isRequired() = false
    private var isValidEmail: Boolean = false

    private val validationHandler: Handler = Handler()
    private var validationRunnable: Runnable? = null

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

        validationRunnable = Runnable {
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
                    editedistener.invoke(input)
                }
            }
        }
        validationHandler.post(validationRunnable)

        viewBinding.inputField.editText?.doAfterTextChanged {
            viewBinding.inputField.error = null
            if (validationRunnable != null) {
                validationHandler.removeCallbacks(validationRunnable)
            }

            // Adding a small delay so users aren't shown an error instantly while typing their emailaddress
            validationHandler.postDelayed(validationRunnable, 400)


            emailAddress = it.toString()
        }

        viewBinding.inputField.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkCompleted()
            }
        }
        checkCompleted()
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is EmailAddressItem && other.emailAddress == emailAddress

    override fun hasSameContentAs(other: Item<*>) =
        other is EmailAddressItem && other.emailAddress == emailAddress

    override fun isCompleted(): Boolean {
        return !emailAddress.isNullOrEmpty() && isValidEmail
    }

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        emailAddress?.let {
            answers.put("email", it)
        }
        return answers
    }
}
