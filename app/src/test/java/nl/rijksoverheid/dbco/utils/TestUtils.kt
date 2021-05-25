/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.utils

import nl.rijksoverheid.dbco.config.*

fun createAppConfig(
    androidMinimumVersionCode: Int = 0,
    androidMinimumVersionMessage: String? = "test",
    symptoms: List<Symptom> = emptyList(),
    guidelines: GuidelinesContainer = GuidelinesContainer(
        introExposureDateKnown = Guidelines(
            category1 = "test",
            category2 = "test",
            category3 = "test"
        ),
        introExposureDateUnknown = GenericGuidelines(
            category1 = "test",
            category2 = "test",
            category3 = "test"
        ),
        guidelinesExposureDateKnown = RangedGuidelines(
            category1 = "test",
            category2 = RangedGuideline(
                withinRange = "test",
                outsideRange = "test"
            ),
            category3 = "test"
        ),
        guidelinesExposureDateUnknown = GenericGuidelines(
            category1 = "test",
            category2 = "test",
            category3 = "test"
        ),
        referenceNumberItem = "test",
        outro = GenericGuidelines(
            category1 = "test",
            category2 = "test",
            category3 = "test"
        )
    ),
    supportedZipCodeRange: List<ZipCodeRange> = emptyList(),
    featureFlags: FeatureFlags = FeatureFlags(
        enableSelfBCO = true,
        enableContactCalling = true,
        enablePerspectiveCopy = true
    )
) = AppConfig(
    androidMinimumVersion = androidMinimumVersionCode,
    androidMinimumVersionMessage = androidMinimumVersionMessage,
    featureFlags = featureFlags,
    symptoms = symptoms,
    supportedZipCodeRanges = supportedZipCodeRange,
    guidelines = guidelines
)