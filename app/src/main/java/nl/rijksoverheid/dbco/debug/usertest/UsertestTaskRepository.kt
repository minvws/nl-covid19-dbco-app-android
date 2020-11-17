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
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.CaseBody
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository.Companion.CASE_KEY
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.user.IUserRepository

class UsertestTaskRepository(context: Context, userInterface: IUserRepository) : ITaskRepository {
    private var cachedCase: Case? = null
    private var encryptedSharedPreferences: SharedPreferences =
        LocalStorageRepository.getInstance(context).getSharedPreferences()

    /**
     * Either get the previously stored task from local storage, or return the mocked response instead
     */
    override suspend fun fetchCase(): Case? {
        if (cachedCase == null) {
            val storedResponse: String = encryptedSharedPreferences.getString(
                CASE_KEY,
                null
            ) ?: MOCKED_CASE_BODY
            val caseBody: CaseBody = Json.decodeFromString(storedResponse)
            cachedCase = caseBody.case
        }
        return cachedCase
    }

    override fun saveChangesToTask(updatedTask: Task) {
        val currentTasks = cachedCase?.tasks as ArrayList
        var found = false
        currentTasks.forEachIndexed { index, currentTask ->
            if (updatedTask.uuid == currentTask.uuid) {
                currentTasks[index] = updatedTask
                found = true
            }
        }
        if (!found) {
            currentTasks.add(updatedTask)
        }

        //Timber.w("Final result is $previousResponse")

        val storeString = ITaskRepository.JSON_SERIALIZER.encodeToString(CaseBody(cachedCase))
        encryptedSharedPreferences.edit().putString(CASE_KEY, storeString).apply()
    }

    override fun getCachedCase(): Case? {
        return cachedCase
    }

    override suspend fun uploadCase() {
        TODO("Not yet implemented")
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

