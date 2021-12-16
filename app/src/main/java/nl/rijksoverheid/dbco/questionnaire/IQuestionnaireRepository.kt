/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.questionnaire

import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire

/**
 * Repository for the [Questionnaire] as shown in Task details
 */
interface IQuestionnaireRepository {

    /**
     * Sync the [Questionnaire] with the back-end
     */
    suspend fun syncQuestionnaires()

    /**
     * @return a cached version of the [Questionnaire] or null when no cached version exist
     */
    fun getCachedQuestionnaire(): Questionnaire?
}