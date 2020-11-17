/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnaire

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire

class QuestionnareRepository(context: Context) : IQuestionnaireRepository {

    private val api = StubbedAPI.create(context)
    private var cachedQuestionnaire: Questionnaire? = null
    private val sharedPrefs =
        context.getSharedPreferences(PREFS_QUESTIONNAIRE, Context.MODE_PRIVATE)

    override suspend fun syncQuestionnaires() {
        if (cachedQuestionnaire != null) {
            return
        }
        val storedQuestionnaire = sharedPrefs.getString(KEY_QUESTIONNAIRE, null)
        if (storedQuestionnaire == null) {
            cachedQuestionnaire = api.getQuestionnaires().questionnaires?.firstOrNull()
            val questionnaireString = Json {
                ignoreUnknownKeys = true
            }.encodeToString(cachedQuestionnaire)
            sharedPrefs.edit().putString(KEY_QUESTIONNAIRE, questionnaireString)
                .apply()
        } else {
            cachedQuestionnaire = Json {
                ignoreUnknownKeys = true
            }.decodeFromString(storedQuestionnaire)
        }
    }

    override fun getCachedQuestionnaire() = cachedQuestionnaire

    companion object {
        const val PREFS_QUESTIONNAIRE = "PREFS_QUESTIONNAIRE"
        const val KEY_QUESTIONNAIRE = "KEY_QUESTIONNAIRE"
    }
}