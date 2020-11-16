/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.tasks.data.entity.Task

interface ITaskRepository {

    suspend fun retrieveCase(): Case?
    fun saveChangesToTask(updatedTask: Task)
    fun getCase(): Case?

    companion object {
        val JSON_SERIALIZER = Json {
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
        }
    }
}