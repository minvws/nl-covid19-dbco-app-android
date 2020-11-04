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
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.TaskInterface
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TasksResponse

class UsertestTaskRepository(context: Context) : TaskInterface {
    private var previousResponse: TasksResponse? = null
    private var encryptedSharedPreferences: SharedPreferences =
        LocalStorageRepository.getInstance(context).getSharedPreferences()

    /**
     * Either get the previously stored task from local storage, or return the mocked response instead
     */
    override suspend fun retrieveTasksForUUID(uuid: String): TasksResponse {
        return if (previousResponse == null) {
            val storedResponse: String = encryptedSharedPreferences.getString(
                "usertasks",
                MOCK_TASKS
            ) ?: MOCK_TASKS
            val taskResponse: TasksResponse = Json.decodeFromString(storedResponse)
            previousResponse = taskResponse
            taskResponse
        } else {
            previousResponse!!
        }
    }

    override fun saveChangesToTask(updatedTask: Task) {
        val currentTasks = previousResponse?.case?.tasks as ArrayList
        var i = 0
        var found = false
        currentTasks.forEach { currentTask ->
            if (updatedTask.uuid == currentTask.uuid) {
                (previousResponse?.case?.tasks as java.util.ArrayList<Task>)[i] = updatedTask
                found = true
            }
            i++
        }
        if (!found) {
            (previousResponse?.case?.tasks as java.util.ArrayList<Task>).add(updatedTask)
        }

        //Timber.w("Final result is $previousResponse")

        val storeString = Json {
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(String.serializer())
                contextual(Int.serializer())
                contextual(Double.serializer())
                contextual(QuestionnaireResult.serializer())
                contextual(JsonArray.serializer())
                contextual(JsonElement.serializer())
            }
        }.encodeToString(previousResponse)
        encryptedSharedPreferences.edit().putString("usertasks", storeString).apply()

    }

    companion object {
        const val MOCK_TASKS = "{\n" +
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

