/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

interface InputQuestionMultipleOptionsItemValidator {

    /**
     * @param input, the text to validate
     * @return Pair containing whether the text is valid and optional error message resource
     */
    fun validate(input: String?): Pair<Boolean, Int?>
}