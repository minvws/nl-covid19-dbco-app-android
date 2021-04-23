package nl.rijksoverheid.dbco.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val androidMinimumVersionMessage: String?,
    val iosMinimumVersion: String?,
    val iosMinimumVersionMessage: String?,
    val androidMinimumVersion: Int,
    val iosAppStoreURL: String?,
    val featureFlags: FeatureFlags,
    val symptoms: List<Symptom>,
    val supportedZipCodeRanges: List<ZipCodeRange>,
    val guidelines: GuidelinesContainer
) {

    fun isSelfBcoSupportedForZipCode(zipCode: Int): Boolean {
        return supportedZipCodeRanges.any { it.contains(zipCode) }
    }
}

@Serializable
data class GuidelinesContainer(
    val introExposureDateKnown: Guidelines,
    val introExposureDateUnknown: GenericGuidelines,
    val guidelinesExposureDateKnown: RangedGuidelines,
    val guidelinesExposureDateUnknown: GenericGuidelines,
    private val referenceNumberItem: String,
    val outro: GenericGuidelines
) {
    fun getReferenceNumberItem(referenceNumber: String): String {
        return referenceNumberItem.replace(Guidelines.REFERENCE_NUMBER, referenceNumber)
    }
}

@Serializable
data class Guidelines(
    private val category1: String,
    private val category2: String,
    private val category3: String
) {

    fun getCategory1(): String = category1

    fun getCategory2(exposureDate: String): String {
        return category2.replace(EXPOSURE_DATE, exposureDate)
    }

    fun getCategory3(exposureDate: String): String {
        return category3.replace(EXPOSURE_DATE, exposureDate)
    }

    companion object {
        const val EXPOSURE_DATE = "{ExposureDate}"
        const val EXPOSURE_DATE_PLUS_FIVE = "{ExposureDate+5}"
        const val EXPOSURE_DATE_PLUS_TEN = "{ExposureDate+10}"
        const val EXPOSURE_DATE_PLUS_ELEVEN = "{ExposureDate+11}"
        const val REFERENCE_NUMBER_ITEM = "{ReferenceNumberItem}"
        const val REFERENCE_NUMBER = "{ReferenceNumber}"
    }
}

@Serializable
data class GenericGuidelines(
    private val category1: String,
    private val category2: String,
    private val category3: String
) {
    fun getCategory1(referenceNumberItem: String? = null): String {
        return category1.replace(Guidelines.REFERENCE_NUMBER_ITEM, referenceNumberItem ?: "")
    }

    fun getCategory2(referenceNumberItem: String? = null): String {
        return category2.replace(Guidelines.REFERENCE_NUMBER_ITEM, referenceNumberItem ?: "")
    }

    fun getCategory3(referenceNumberItem: String? = null): String {
        return category3.replace(Guidelines.REFERENCE_NUMBER_ITEM, referenceNumberItem ?: "")
    }
}

@Serializable
data class RangedGuidelines(
    private val category1: String,
    private val category2: RangedGuideline,
    private val category3: String
) {
    fun getCategory1(exposureDatePlusEleven: String, referenceNumberItem: String? = null): String {
        return category1
            .replace(Guidelines.EXPOSURE_DATE_PLUS_ELEVEN, exposureDatePlusEleven)
            .replace(Guidelines.REFERENCE_NUMBER_ITEM, referenceNumberItem ?: "")
    }

    fun getCategory2(
        withinRange: Boolean,
        exposureDatePlusFive: String,
        exposureDatePlusTen: String,
        referenceNumberItem: String? = null
    ): String {
        val text = if (withinRange) category2.withinRange else category2.outsideRange
        return text
            .replace(Guidelines.EXPOSURE_DATE_PLUS_FIVE, exposureDatePlusFive)
            .replace(Guidelines.EXPOSURE_DATE_PLUS_TEN, exposureDatePlusTen)
            .replace(Guidelines.REFERENCE_NUMBER_ITEM, referenceNumberItem ?: "")
    }

    fun getCategory3(
        exposureDatePlusFive: String,
        referenceNumberItem: String? = null
    ): String {
        return category3
            .replace(Guidelines.EXPOSURE_DATE_PLUS_FIVE, exposureDatePlusFive)
            .replace(Guidelines.REFERENCE_NUMBER_ITEM, referenceNumberItem ?: "")
    }
}

@Serializable
data class RangedGuideline(
    val withinRange: String,
    val outsideRange: String
)

@Serializable
data class FeatureFlags(
    val enableContactCalling: Boolean,
    val enablePerspectiveCopy: Boolean,
    val enableSelfBCO: Boolean
)

@Serializable
data class Symptom(
    val label: String,
    val value: String
)

@Serializable
data class ZipCodeRange(
    val start: Int,
    val end: Int
) {

    fun contains(zipCode: Int): Boolean {
        return zipCode in start..end
    }
}
