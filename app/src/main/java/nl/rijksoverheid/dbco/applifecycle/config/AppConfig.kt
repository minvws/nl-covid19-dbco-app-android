package nl.rijksoverheid.dbco.applifecycle.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val androidMinimumVersionMessage: String? = null,
    val iosMinimumVersion: String? = null,
    val iosMinimumVersionMessage: String? = null,
    val androidMinimumVersion: Int = 0,
    val iosAppStoreURL: String? = null,
    val featureFlags: FeatureFlags = FeatureFlags(),
    val symptoms: List<Symptom> = listOf(
        Symptom(label = "Neusverkoudheid", value = "nasal-cold"),
        Symptom(label = "Schorre stem", value = "hoarse-voice"),
        Symptom(label = "Keelpijn", value = "sore-throat"),
        Symptom(label = "(licht) hoesten", value = "cough"),
        Symptom(label = "Kortademigheid/benauwdheid", value = "shortness-of-breath"),
        Symptom(label = "Pijn bij de ademhaling", value = "painful-breathing"),
        Symptom(label = "Koorts (= boven 38 graden Celsius)", value = "fever"),
        Symptom(label = "Koude rillingen", value = "cold-shivers"),
        Symptom(label = "Verlies van of verminderde reuk", value = "loss-of-smell"),
        Symptom(label = "Verlies van of verminderde smaak", value = "loss-of-taste"),
        Symptom(label = "Algehele malaise", value = "malaise"),
        Symptom(label = "Vermoeidheid", value = "fatigue"),
        Symptom(label = "Hoofdpijn", value = "headache"),
        Symptom(label = "Spierpijn", value = "muscle-strain"),
        Symptom(label = "Pijn achter de ogen", value = "pain-behind-the-eyes"),
        Symptom(label = "Algehele pijnklachten", value = "pain"),
        Symptom(label = "Duizeligheid", value = "dizziness"),
        Symptom(label = "Prikkelbaar/verwardheid", value = "irritable-confused"),
        Symptom(label = "Verlies van eetlust", value = "loss-of-appetite"),
        Symptom(label = "Misselijkheid", value = "nausea"),
        Symptom(label = "Overgeven", value = "vomiting"),
        Symptom(label = "Diarree", value = "diarrhea"),
        Symptom(label = "Buikpijn", value = "stomach-ache"),
        Symptom(label = "Rode prikkende ogen (oogontsteking)", value = "pink-eye"),
        Symptom(label = "Huidafwijkingen", value = "skin-condition")
    ),
    val supportedZipRanges: List<ZipRange> = listOf(
        ZipRange(start = 1400, end = 1500)
    )
) {

    fun isSelfBcoSupportedForZipCode(zipCode: Int): Boolean {
        return supportedZipRanges.any { it.contains(zipCode) }
    }
}

@Serializable
data class FeatureFlags(
    val enableContactCalling: Boolean = false,
    val enablePerspectiveSharing: Boolean = false,
    val enablePerspectiveCopy: Boolean = false,
    val enableSelfBCO: Boolean = false
)

@Serializable
data class Symptom(
    val label: String,
    val value: String
)

@Serializable
data class ZipRange(
    val start: Int,
    val end: Int
) {

    fun contains(zipCode: Int): Boolean {
        return zipCode in start..end
    }
}
