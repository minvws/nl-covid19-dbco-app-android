/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.config

import kotlinx.serialization.Serializable

/**
 * Dynamic configuration used throughout the app
 */
@Serializable
data class AppConfig(

    /**
     * The message to show when the current version of the app is not supported anymore
     */
    val androidMinimumVersionMessage: String?,

    /**
     * The minimum version code of the app which is supported by the back-end
     */
    val androidMinimumVersion: Int,

    /**
     * Current feature flag values, used to disable/enable some features in the app
     */
    val featureFlags: FeatureFlags,

    /**
     * List of possible symptoms an index might have and select
     */
    val symptoms: List<Symptom>,

    /**
     * Guidelines specific for risk categories to show for a given [Task]
     */
    val guidelines: GuidelinesContainer
)

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
