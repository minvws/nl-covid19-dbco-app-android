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

class QuestionnareRepository(context: Context) : IQuestionnaireRepository {

    private val api = DbcoApi.create(context)
    private var cachedQuestionnaire: Questionnaire? = null
    private val sharedPrefs by lazy { LocalStorageRepository.getInstance(context).getSharedPreferences() }


    override suspend fun syncQuestionnaires() {
        if (cachedQuestionnaire != null) {
            return
        }
        val storedQuestionnaire = sharedPrefs.getString(KEY_QUESTIONNAIRE, null)
        if (storedQuestionnaire == null) {
            try {
                cachedQuestionnaire = api.getQuestionnaires().questionnaires?.firstOrNull()
                val questionnaireString =  Defaults.json.encodeToString(cachedQuestionnaire)
                sharedPrefs.edit().putString(KEY_QUESTIONNAIRE, questionnaireString)
                    .apply()
            } catch (ex: Exception) {
                Timber.e(ex, "Error while fetching or parsing questionnary")
            }
        } else {
            cachedQuestionnaire =  Defaults.json.decodeFromString(storedQuestionnaire)
        }
    }

    override fun getCachedQuestionnaire() = cachedQuestionnaire

    companion object {
        const val PREFS_QUESTIONNAIRE = "PREFS_QUESTIONNAIRE"
        const val KEY_QUESTIONNAIRE = "KEY_QUESTIONNAIRE"
    }
}