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
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import timber.log.Timber

class QuestionnaireRepository(context: Context) : IQuestionnaireRepository {

    private val api by lazy {
        DbcoApi.create(context)
    }

    private val sharedPrefs by lazy {
        LocalStorageRepository.getInstance(context).getSharedPreferences()
    }

    override suspend fun syncQuestionnaires() {
        if (sharedPrefs.getString(KEY_QUESTIONNAIRE, null) == null) {
            val cachedQuestionnaire = api.getQuestionnaires().questionnaires?.firstOrNull()
            val questionnaireString = Defaults.json.encodeToString(cachedQuestionnaire)
            sharedPrefs.edit().putString(KEY_QUESTIONNAIRE, questionnaireString).apply()
        }
    }

    override fun getCachedQuestionnaire(): Questionnaire? {
        return sharedPrefs.getString(KEY_QUESTIONNAIRE, null)?.let { questionnaire ->
            Defaults.json.decodeFromString(questionnaire)
        }
    }

    companion object {
        const val KEY_QUESTIONNAIRE = "KEY_QUESTIONNAIRE"
    }
}