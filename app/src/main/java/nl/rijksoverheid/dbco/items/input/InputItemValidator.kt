/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import androidx.annotation.StringRes

interface InputItemValidator {

    /**
     * @param input, the text to validate
     * @return validation result
     */
    fun validate(input: String?): InputValidationResult
}

sealed class InputValidationResult {

    data class Valid(val isComplete: Boolean) : InputValidationResult()
    data class Warning(@StringRes val warningRes: Int) : InputValidationResult()
    data class Error(@StringRes val errorRes: Int) : InputValidationResult()
}