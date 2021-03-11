/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.debug.usertest

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.CaseBody
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository.Companion.CASE_KEY
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import org.joda.time.LocalDate

class UsertestTaskRepository(context: Context) : ITaskRepository {

    private var case: Case

    private var encryptedSharedPreferences: SharedPreferences =
        LocalStorageRepository.getInstance(context).getSharedPreferences()

    init {
        /**
         * Either get the previously stored task from local storage, or return the mocked response instead
         */
        val storedResponse: String = encryptedSharedPreferences.getString(
            CASE_KEY,
            null
        ) ?: MOCKED_CASE_BODY
        val caseBody: CaseBody = Json.decodeFromString(storedResponse)
        case = caseBody.case!!
    }

    override suspend fun fetchCase(): Case = case

    override fun getCaseReference(): String? = case.reference

    override fun saveTask(updatedTask: Task, shouldMerge: (Task) -> Boolean) {
        val tasks = case.tasks.toMutableList()
        var found = false
        tasks.forEachIndexed { index, currentTask ->
            if (shouldMerge(currentTask)) {
                tasks[index] = updatedTask
                found = true
            }
        }
        if (!found) {
            tasks.add(updatedTask)
        }
        case = case.copy(tasks = tasks)
        val storeString = Defaults.json.encodeToString(CaseBody(case))
        encryptedSharedPreferences.edit().putString(CASE_KEY, storeString).apply()
    }

    override fun deleteTask(taskToDelete: Task) {
        // NO-OP
    }

    override fun getCase(): Case = case

    override suspend fun uploadCase() {
        // NO-OP
    }

    override fun getSymptomOnsetDate(): String? = case.dateOfSymptomOnset

    override fun getStartOfContagiousPeriod(): LocalDate? {
        return if (case.dateOfSymptomOnset == null && case.dateOfTest == null) {
            null
        } else {
            case.dateOfSymptomOnset?.let {
                LocalDate.parse(it, DateFormats.dateInputData).minusDays(2)
            } ?: LocalDate.parse(case.dateOfTest, DateFormats.dateInputData)
        }
    }

    override fun updateSymptomOnsetDate(dateOfSymptomOnset: String) {
        case = case.copy(dateOfSymptomOnset = dateOfSymptomOnset)
    }

    override fun updateTestDate(testDate: String) {
        case = case.copy(dateOfTest = testDate)
    }

    override fun getTestDate(): String? = case.dateOfTest

    override fun addSymptom(symptom: String) {
        val symptoms = case.symptoms.toMutableSet()
        symptoms.add(symptom)
        case = case.copy(symptoms = symptoms)
    }

    override fun removeSymptom(symptom: String) {
        val symptoms = case.symptoms.toMutableSet()
        symptoms.remove(symptom)
        case = case.copy(symptoms = symptoms)
    }

    override fun getSymptoms(): List<String> {
        return case.symptoms.toList()
    }

    override fun ifCaseWasChanged(): Boolean = true

    companion object {
        const val MOCKED_CASE_BODY = "{\n" +
                "  \"case\": {\n" +
                "      \"dateOfSymptomOnset\": \"2020-10-29\",\n" +
                "      \"tasks\": [\n" +
                "        {\n" +
                "          \"uuid\": \"123e4567-e89b-12d3-a456-426614172000\",\n" +
                "          \"taskType\": \"contact\",\n" +
                "          \"source\": \"portal\",\n" +
                "          \"label\": \"Huisgenoot 1\",\n" +
                "          \"taskContext\": null,\n" +
                "          \"category\": \"1\",\n" +
                "          \"communication\": \"staff\",\n" +
                "          \"dateOfLastExposure\": \"2020-10-30\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"uuid\": \"123e4567-e89b-22d3-a456-426614172000\",\n" +
                "          \"taskType\": \"contact\",\n" +
                "          \"source\": \"portal\",\n" +
                "          \"label\": \"Huisgenoot 2\",\n" +
                "          \"taskContext\": null,\n" +
                "          \"category\": \"1\",\n" +
                "          \"communication\": \"staff\",\n" +
                "          \"dateOfLastExposure\": \"2020-10-30\"\n" +
                "        }\n" +
                "      ]\n" +
                "  }\n" +
                "}"
    }
}

