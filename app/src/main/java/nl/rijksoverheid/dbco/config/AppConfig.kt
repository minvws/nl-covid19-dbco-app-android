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
    val supportedZipCodeRanges: List<ZipCodeRange>
) {

    fun isSelfBcoSupportedForZipCode(zipCode: Int): Boolean {
        return supportedZipCodeRanges.any { it.contains(zipCode) }
    }
}

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
