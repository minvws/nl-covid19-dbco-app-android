package nl.rijksoverheid.dbco.items.input

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.dbco.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.InputStream
import java.util.stream.Collectors

@RunWith(MockitoJUnitRunner::class)
class NameValidatorTest {

    @Test
    fun `given input is empty, when input is validated, then return valid result but incomplete`() {
        // given
        val context = mockk<Context>()
        val input = ""
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream()
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream()

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertEquals(
            validator.validate(input),
            InputValidationResult.Valid(isComplete = false)
        )
    }

    @Test
    fun `given input is not empty with normal name, when input is validated, then return valid result but incomplete`() {
        // given
        val context = mockk<Context>()
        val input = "niels"
        val invalidNames = listOf("mama", "papa")
        val invalidSuffixes = listOf("je", "weg")
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream(
            invalidSuffixes
        )
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream(
            invalidNames
        )

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertEquals(
            validator.validate(input),
            InputValidationResult.Valid(isComplete = false)
        )
    }

    @Test
    fun `given input is not empty with normal name with uppercase, when input is validated, then return valid result but incomplete`() {
        // given
        val context = mockk<Context>()
        val input = "Niels"
        val invalidNames = listOf("mama", "papa")
        val invalidSuffixes = listOf("je", "weg")
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream(
            invalidSuffixes
        )
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream(
            invalidNames
        )

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertEquals(
            validator.validate(input),
            InputValidationResult.Valid(isComplete = false)
        )
    }

    @Test
    fun `given input is not empty with only vowels, when input is validated, then return warning result`() {
        // given
        val context = mockk<Context>()
        val input = "aoy"
        val invalidNames = listOf("mama", "papa")
        val invalidSuffixes = listOf("je", "weg")
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream(
            invalidSuffixes
        )
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream(
            invalidNames
        )

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertTrue(validator.validate(input) is InputValidationResult.Warning)
    }

    @Test
    fun `given input is not empty with only consonants, when input is validated, then return warning result`() {
        // given
        val context = mockk<Context>()
        val input = "mnp"
        val invalidNames = listOf("mama", "papa")
        val invalidSuffixes = listOf("je", "weg")
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream(
            invalidSuffixes
        )
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream(
            invalidNames
        )

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertTrue(validator.validate(input) is InputValidationResult.Warning)
    }

    @Test
    fun `given input is not empty with only invalid character, when input is validated, then return warning result`() {
        // given
        val context = mockk<Context>()
        val input = "Niels$"
        val invalidNames = listOf("mama", "papa")
        val invalidSuffixes = listOf("je", "weg")
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream(
            invalidSuffixes
        )
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream(
            invalidNames
        )

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertTrue(validator.validate(input) is InputValidationResult.Warning)
    }

    @Test
    fun `given input is not empty but contains invalid word, when input is validated, then return warning result`() {
        // given
        val context = mockk<Context>()
        val input = "mama"
        val invalidNames = listOf("mama", "papa")
        val invalidSuffixes = listOf("je", "weg")
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream(
            invalidSuffixes
        )
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream(
            invalidNames
        )

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertTrue(validator.validate(input) is InputValidationResult.Warning)
    }

    @Test
    fun `given input is not empty but ends with invalid suffix, when input is validated, then return warning result`() {
        // given
        val context = mockk<Context>()
        val input = "nielsje"
        val invalidNames = listOf("mama", "papa")
        val invalidSuffixes = listOf("je", "weg")
        every { context.resources.openRawResource(R.raw.invalid_name_suffixes) } returns stringsInputStream(
            invalidSuffixes
        )
        every { context.resources.openRawResource(R.raw.invalid_names) } returns stringsInputStream(
            invalidNames
        )

        // when
        val validator = ContactNameItem.NameValidator(context)

        // then
        Assert.assertTrue(validator.validate(input) is InputValidationResult.Warning)
    }

    private fun stringsInputStream(strings: List<String> = emptyList()): InputStream {
        return strings
            .stream()
            .collect(Collectors.joining("\n", "", "\n"))
            .byteInputStream()
    }
}