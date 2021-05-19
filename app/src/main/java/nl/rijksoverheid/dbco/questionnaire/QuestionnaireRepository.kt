/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnaire

import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire

class QuestionnaireRepository(
    private val storage: SharedPreferences,
    private val api: DbcoApi
) : IQuestionnaireRepository {

    override suspend fun syncQuestionnaires() {
        if (storage.getString(KEY_QUESTIONNAIRE, null) == null) {
            val cachedQuestionnaire = api.getQuestionnaires().questionnaires?.firstOrNull()
            val questionnaireString = Defaults.json.encodeToString(cachedQuestionnaire)
            storage.edit().putString(KEY_QUESTIONNAIRE, questionnaireString).apply()
        }
    }

    override fun getCachedQuestionnaire(): Questionnaire? {
        return storage.getString(KEY_QUESTIONNAIRE, null)?.let { questionnaire ->
            Defaults.json.decodeFromString(questionnaire)
        }
    }

    companion object {
        const val KEY_QUESTIONNAIRE = "KEY_QUESTIONNAIRE"
    }
}