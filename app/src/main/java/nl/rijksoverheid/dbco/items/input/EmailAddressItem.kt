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
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Warning
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Error
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Valid

class EmailAddressItem(
    emailAddresses: Set<String>,
    question: Question?,
    isEnabled: Boolean,
    canShowEmptyWarning: Boolean = false,
    changeListener: (Set<String>) -> Unit
) : InputQuestionMultipleOptionsItem(
    question = question,
    items = emailAddresses,
    validator = EmailAddressValidator(canShowEmptyWarning),
    changeListener = changeListener,
    key = ANSWER_KEY,
    type = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
    singleHint = R.string.hint_email_address,
    multipleHint = R.string.hint_email_address_multiple,
    isEnabled = isEnabled
) {

    internal class EmailAddressValidator(
        private val canShowEmptyWarning: Boolean
    ) : InputItemValidator {

        override fun validate(input: String?): InputValidationResult {
            return if (input.isNullOrEmpty()) {
                return if (canShowEmptyWarning) {
                    Warning(warningRes = R.string.warning_necessary)
                } else {
                    Valid(isComplete = false)
                }
            } else {
                val matches = Patterns.EMAIL_ADDRESS.matcher(input).matches()
                if (matches) {
                    Valid(isComplete = true)
                } else {
                    Error(errorRes = R.string.error_valid_email)
                }
            }
        }
    }

    companion object {

        private const val ANSWER_KEY = "email"
    }
}

