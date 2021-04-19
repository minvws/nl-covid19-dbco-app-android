package nl.rijksoverheid.dbco.config

import org.junit.Assert
import org.junit.Test

class ConfigTests {

    @Test
    fun `given a zipcode, when exist in range, then return self bco supported`() {
        // given
        val config = AppConfig(
            supportedZipCodeRanges = listOf(
                ZipCodeRange(start = 1400, end = 1500),
                ZipCodeRange(start = 2000, end = 2100)
            )
        )
        val zipCode = 1450

        // then
        Assert.assertTrue(config.isSelfBcoSupportedForZipCode(zipCode))
    }

    @Test
    fun `given a zipcode, when it is start of range range, then return self bco supported`() {
        // given
        val config = AppConfig(
            supportedZipCodeRanges = listOf(
                ZipCodeRange(start = 1400, end = 1500),
                ZipCodeRange(start = 2000, end = 2100)
            )
        )
        val zipCode = 1400

        // then
        Assert.assertTrue(config.isSelfBcoSupportedForZipCode(zipCode))
    }

    @Test
    fun `given a zipcode, when it is end of range, then return self bco supported`() {
        // given
        val config = AppConfig(
            supportedZipCodeRanges = listOf(
                ZipCodeRange(start = 1400, end = 1500),
                ZipCodeRange(start = 2000, end = 2100)
            )
        )
        val zipCode = 2100

        // then
        Assert.assertTrue(config.isSelfBcoSupportedForZipCode(zipCode))
    }

    @Test
    fun `given a zipcode, when does not exist in range, then return self bco not supported`() {
        // given
        val config = AppConfig(
            supportedZipCodeRanges = listOf(
                ZipCodeRange(start = 1400, end = 1500),
                ZipCodeRange(start = 2000, end = 2100)
            )
        )
        val zipCode = 3600

        // then
        Assert.assertFalse(config.isSelfBcoSupportedForZipCode(zipCode))
    }
}