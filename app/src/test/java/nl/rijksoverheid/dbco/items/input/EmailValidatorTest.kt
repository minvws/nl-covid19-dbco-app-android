/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EmailValidatorTest {

    @Test
    fun `given input is empty and warning cannot be shown, when input is validated, then return valid result but incomplete`() {
        // given
        val input = ""

        // when
        val validator = createValidator(canShowEmptyWarning = false)

        // then
        Assert.assertEquals(
            validator.validate(input),
            InputValidationResult.Valid(isComplete = false)
        )
    }

    @Test
    fun `given input is empty and warning can be shown, when input is validated, then return warning`() {
        // given
        val input = ""

        // when
        val validator = createValidator(canShowEmptyWarning = true)

        // then
        Assert.assertTrue(validator.validate(input) is InputValidationResult.Warning)
    }

    private fun createValidator(
        canShowEmptyWarning: Boolean = true
    ) = EmailAddressItem.EmailAddressValidator(
        canShowEmptyWarning = canShowEmptyWarning
    )
}