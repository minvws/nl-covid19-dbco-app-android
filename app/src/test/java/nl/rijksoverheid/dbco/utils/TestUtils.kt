package nl.rijksoverheid.dbco.utils

import nl.rijksoverheid.dbco.config.*

fun createAppConfig(androidMinimumVersionCode: Int = 0) = AppConfig(
    androidMinimumVersion = androidMinimumVersionCode,
    androidMinimumVersionMessage = "test",
    featureFlags = FeatureFlags(
        enableSelfBCO = true,
        enablePerspectiveCopy = true,
        enableContactCalling = true
    ),
    symptoms = emptyList(),
    supportedZipCodeRanges = emptyList(),
    guidelines = GuidelinesContainer(
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
    )
)