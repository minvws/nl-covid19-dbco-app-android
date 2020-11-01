/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnaire

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.network.StubbedAPI

class QuestionnareRepository(context: Context) : QuestionnaireInterface {

    private val api = StubbedAPI.create(context)
    private var questionnaireToUse: ContactDetailsResponse? = null

    override suspend fun retrieveQuestionnaires(): ContactDetailsResponse {
        return if (questionnaireToUse == null) {
            val data = withContext(Dispatchers.IO) { api.getQuestionnaires() }
            questionnaireToUse = data.body()
            data.body()!!

        } else {
            questionnaireToUse!!
        }
    }
}