/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.InputType
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Warning
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Error
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Valid

class PhoneNumberItem(
    numbers: Set<String>,
    question: Question?,
    isEnabled: Boolean,
    canShowEmptyWarning: Boolean = false,
    changeListener: (Set<String>) -> Unit
) : InputQuestionMultipleOptionsItem(
    question = question,
    items = numbers,
    validator = PhoneNumberValidator(canShowEmptyWarning),
    changeListener = changeListener,
    key = ANSWER_KEY,
    type = InputType.TYPE_CLASS_PHONE,
    singleHint = R.string.hint_phone_number,
    multipleHint = R.string.hint_phone_number_multiple,
    isEnabled = isEnabled
) {

    internal class PhoneNumberValidator(
        private val canShowEmptyWarning: Boolean
    ) : InputItemValidator {

        override fun validate(input: String?): InputValidationResult {
            if (input.isNullOrEmpty()) {
                return if (canShowEmptyWarning) {
                    Warning(warningRes = R.string.warning_necessary)
                } else {
                    Valid(isComplete = false)
                }
            }

            val replaced = input.replace(Regex("[\\s)(]"), "")

            if (!Constants.PHONE_VALIDATION_MATCHER.matcher(replaced).matches()) {
                // If the matcher fails for whatever reason, check if input was too long or too short
                return when {
                    replaced.length < 10 -> Error(errorRes = R.string.error_valid_phone_too_short)
                    replaced.length > 11 -> Error(errorRes = R.string.error_valid_phone_too_long)
                    else -> Error(errorRes = R.string.error_valid_phone)
                }

            } else {
                // Special case for numbers of length 11 to 13 but with valid syntax
                return if (replaced.length in 11..13) {
                    // If its not a number starting with our valid prefixes, don't allow it
                    if (!Constants.VALID_PHONENUMER_PREFIXES.any { input.startsWith(it) }) {
                        Error(errorRes = R.string.error_valid_phone)
                    } else {
                        Valid(isComplete = true)
                    }
                } else {
                    Valid(isComplete = true)
                }
            }
        }
    }

    companion object {

        private const val ANSWER_KEY = "phoneNumber"
    }
}