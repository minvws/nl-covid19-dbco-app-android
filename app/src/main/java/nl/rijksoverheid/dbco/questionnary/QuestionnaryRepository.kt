/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnary

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.network.StubbedAPI
import retrofit2.Response

class QuestionnaryRepository(context: Context) {

    private val api = StubbedAPI.create(context)

    suspend fun retrieveQuestionnairy(): Response<ContactDetailsResponse> {
        return withContext(Dispatchers.IO) {
            return@withContext withContext(Dispatchers.Default) { api.getQuestionnaires() }
        }
    }
}