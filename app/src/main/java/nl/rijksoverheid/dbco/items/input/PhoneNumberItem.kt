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

class PhoneNumberItem(
    numbers: Set<String>,
    question: Question?,
    isEnabled: Boolean,
    changeListener: (Set<String>) -> Unit
) : InputQuestionMultipleOptionsItem(
    question = question,
    items = numbers,
    validator = PhoneNumberValidator(),
    changeListener = changeListener,
    key = ANSWER_KEY,
    type = InputType.TYPE_CLASS_PHONE,
    singleHint = R.string.hint_phone_number,
    multipleHint = R.string.hint_phone_number_multiple,
    isEnabled = isEnabled
) {

    internal class PhoneNumberValidator : InputQuestionMultipleOptionsItemValidator {

        override fun validate(input: String?): Pair<Boolean, Int?> {
            if (input.isNullOrEmpty()) return Pair(false, null)

            val replaced = input.replace(Regex("[\\s)(]"), "")

            if (!Constants.PHONE_VALIDATION_MATCHER.matcher(replaced).matches()) {
                // If the matcher fails for whatever reason, check if input was too long or too short
                return when {
                    replaced.length < 10 -> Pair(false, R.string.error_valid_phone_too_short)
                    replaced.length > 11 -> Pair(false, R.string.error_valid_phone_too_long)
                    else -> Pair(false, R.string.error_valid_phone)
                }

            } else {
                // Special case for numbers of length 11 to 13 but with valid syntax
                return if (replaced.length in 11..13) {
                    // If its not a number starting with our valid prefixes, don't allow it
                    if (!Constants.VALID_PHONENUMER_PREFIXES.any { input.startsWith(it) }) {
                        Pair(false, R.string.error_valid_phone)
                    } else {
                        Pair(true, null)
                    }
                } else {
                    Pair(true, null)
                }
            }
        }
    }

    companion object {

        private const val ANSWER_KEY = "phoneNumber"
    }
}