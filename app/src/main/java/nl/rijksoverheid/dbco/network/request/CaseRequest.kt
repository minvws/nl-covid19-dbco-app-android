package nl.rijksoverheid.dbco.network.request

import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.bcocase.data.entity.Case
import org.joda.time.LocalDate

@Serializable
data class CaseRequest(
    val reference: String?,
    val dateOfSymptomOnset: String?,
    val dateOfTest: String?,
    val windowExpiresAt: String?,
    val tasks: List<TaskRequest> = mutableListOf(),
    val symptoms: Set<String> = mutableSetOf(),
    val symptomsKnown: Boolean = false
) {

    companion object {

        fun fromCase(case: Case): CaseRequest = CaseRequest(
            reference = case.reference,
            dateOfSymptomOnset = determineSymptomOnset(case),
            dateOfTest = case.dateOfTest,
            windowExpiresAt = case.windowExpiresAt,
            tasks = case.tasks.map { TaskRequest.fromTask(it) },
            symptoms = case.symptoms,
            symptomsKnown = case.symptomsKnown
        )

        /**
         * Symptom onset as far as the portal is concerned is the most recent date
         * between all possible dates entered by the index
         */
        private fun determineSymptomOnset(case: Case): String? {
            return listOfNotNull(
                case.dateOfSymptomOnset.dateOrNull(),
                case.dateOfIncreasedSymptoms.dateOrNull(),
                case.dateOfNegativeTest.dateOrNull(),
                case.dateOfPositiveTest.dateOrNull()
            ).maxOrNull()?.toString(DateFormats.dateInputData)
        }

        private fun String?.dateOrNull(): LocalDate? {
            return this?.let { LocalDate.parse(it, DateFormats.dateInputData) }
        }
    }
}