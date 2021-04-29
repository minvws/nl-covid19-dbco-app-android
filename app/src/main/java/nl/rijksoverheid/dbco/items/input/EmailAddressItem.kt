/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.InputType
import android.util.Patterns
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question

class EmailAddressItem(
    emailAddresses: Set<String>,
    question: Question?,
    isEnabled: Boolean,
    changeListener: (Set<String>) -> Unit
) : InputQuestionMultipleOptionsItem(
    question = question,
    items = emailAddresses,
    validator = EmailAddressValidator(),
    changeListener = changeListener,
    key = ANSWER_KEY,
    type = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
    singleHint = R.string.hint_email_address,
    multipleHint = R.string.hint_email_address_multiple,
    isEnabled = isEnabled
) {

    internal class EmailAddressValidator : InputItemValidator {

        override fun validate(input: String?): InputValidationResult {
            return if (input.isNullOrEmpty()) {
                return InputValidationResult.Valid(isComplete = false)
            } else {
                val matches = Patterns.EMAIL_ADDRESS.matcher(input).matches()
                if (matches) {
                    InputValidationResult.Valid(isComplete = true)
                } else {
                    InputValidationResult.Error(errorRes = R.string.error_valid_email)
                }
            }
        }
    }

    companion object {

        private const val ANSWER_KEY = "email"
    }
}

