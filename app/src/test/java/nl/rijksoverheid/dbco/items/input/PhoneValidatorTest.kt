package nl.rijksoverheid.dbco.items.input

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PhoneValidatorTest {

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

    @Test
    fun `given input is not empty with valid phone number, when input is validated, then return valid and complete result and complete`() {
        // given
        val input = "0612345678"

        // when
        val validator = createValidator()

        // then
        Assert.assertEquals(
            validator.validate(input),
            InputValidationResult.Valid(isComplete = true)
        )
    }

    @Test
    fun `given input is not empty with valid phone number starting with +, when input is validated, then return valid and complete result and complete`() {
        // given
        val dutchInput = "+31612345678"
        val belgianInput = "+32612345678"
        val germanInput = "+49612345678"

        // when
        val validator = createValidator()
        val results = listOf(
            validator.validate(dutchInput),
            validator.validate(belgianInput),
            validator.validate(germanInput),
        )

        // then
        Assert.assertFalse(results.any { it != InputValidationResult.Valid(isComplete = true) })
    }

    @Test
    fun `given input is not empty with valid phone number starting with 00, when input is validated, then return valid and complete result and complete`() {
        // given
        val dutchInput = "0031612345678"
        val belgianInput = "0032612345678"
        val germanInput = "0049612345678"

        // when
        val validator = createValidator()
        val results = listOf(
            validator.validate(dutchInput),
            validator.validate(belgianInput),
            validator.validate(germanInput),
        )

        // then
        Assert.assertFalse(results.any { it != InputValidationResult.Valid(isComplete = true) })
    }

    @Test
    fun `given input is not empty with valid phone number starting with +00, when input is validated, then return valid and complete result and complete`() {
        // given
        val dutchInput = "+0031612345678"
        val belgianInput = "+0032612345678"
        val germanInput = "+0049612345678"

        // when
        val validator = createValidator()
        val results = listOf(
            validator.validate(dutchInput),
            validator.validate(belgianInput),
            validator.validate(germanInput),
        )

        // then
        Assert.assertFalse(results.any { it != InputValidationResult.Valid(isComplete = true) })
    }

    @Test
    fun `given input is not empty with invalid phone number, when input is validated, then return error`() {
        // given
        val input = "0683223"

        // when
        val validator = createValidator()

        // then
        Assert.assertTrue(validator.validate(input) is InputValidationResult.Error)
    }

    private fun createValidator(
        canShowEmptyWarning: Boolean = true
    ) = PhoneNumberItem.PhoneNumberValidator(
        canShowEmptyWarning = canShowEmptyWarning
    )
}