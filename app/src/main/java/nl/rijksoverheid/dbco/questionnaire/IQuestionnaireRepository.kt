/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.questionnaire

import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire

interface IQuestionnaireRepository {
    suspend fun syncQuestionnaires()
    fun getCachedQuestionnaire(): Questionnaire?

}