/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnaire

import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.serialization.encodeToString
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.contacts.data.entity.QuestionnaireResponse
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.questionnaire.QuestionnaireRepository.Companion.KEY_QUESTIONNAIRE
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QuestionnaireRepositoryTest {

    @Test
    fun `given storage is not empty, when syncing questionnaire, do nothing`() = runBlockingTest {
        // given
        val mockStorage = mockk<SharedPreferences>()
        val mockApi = mockk<DbcoApi>()
        every { mockStorage.getString(KEY_QUESTIONNAIRE, null) } returns "test"

        // when
        val repo = createRepository(mockStorage, mockApi)
        repo.syncQuestionnaires()

        // then
        coVerify(exactly = 0) { mockApi.getQuestionnaires() }
        verify(exactly = 0) { mockStorage.edit().putString(KEY_QUESTIONNAIRE, any()) }
    }

    @Test
    fun `given storage is empty, when syncing questionnaire, fetch from api and store questionnaire`() =
        runBlockingTest {
            // given
            val questionnaire = Questionnaire(uuid = "test")
            val questionnaireResponse = QuestionnaireResponse(
                questionnaires = listOf(
                    questionnaire
                )
            )
            val questionnaireString = Defaults.json.encodeToString(questionnaire)
            val mockStorage = mockk<SharedPreferences>()
            val mockApi = mockk<DbcoApi>()
            every { mockStorage.getString(KEY_QUESTIONNAIRE, null) } returns null
            every {
                mockStorage.edit().putString(KEY_QUESTIONNAIRE, questionnaireString).apply()
            } just Runs
            coEvery { mockApi.getQuestionnaires() } returns questionnaireResponse

            // when
            val repo = createRepository(mockStorage, mockApi)
            repo.syncQuestionnaires()

            // then
            verify { mockStorage.edit().putString(KEY_QUESTIONNAIRE, questionnaireString) }
        }

    private fun createRepository(
        storage: SharedPreferences,
        api: DbcoApi
    ) = QuestionnaireRepository(
        storage,
        api
    )
}