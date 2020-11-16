/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnaire

import android.content.Context
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire

class QuestionnareRepository(context: Context) : IQuestionnaireRepository {

    private val api = StubbedAPI.create(context)
    private var cachedQuestionnaire: Questionnaire? = null

    override suspend fun syncQuestionnaires() {
        cachedQuestionnaire = api.getQuestionnaires().questionnaires?.firstOrNull() // TODO cache in shared prefs
    }

    override fun getCachedQuestionnaire() = cachedQuestionnaire

}