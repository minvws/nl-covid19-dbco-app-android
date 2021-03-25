package nl.rijksoverheid.dbco.network.request

import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.entity.Case

@Serializable
data class CaseRequest(
    val reference: String?,
    val dateOfSymptomOnset: String?,
    val dateOfTest: String?,
    val windowExpiresAt: String?,
    val tasks: List<TaskRequest> = mutableListOf(),
    val symptoms: Set<String> = mutableSetOf(),
    val contagiousPeriodKnown: Boolean = false
) {

    companion object {

        fun fromCase(case: Case): CaseRequest = CaseRequest(
            reference = case.reference,
            dateOfSymptomOnset = case.dateOfSymptomOnset,
            dateOfTest = case.dateOfTest,
            windowExpiresAt = case.windowExpiresAt,
            tasks = case.tasks.map { TaskRequest.fromTask(it) },
            symptoms = case.symptoms,
            contagiousPeriodKnown = case.contagiousPeriodKnown
        )
    }
}