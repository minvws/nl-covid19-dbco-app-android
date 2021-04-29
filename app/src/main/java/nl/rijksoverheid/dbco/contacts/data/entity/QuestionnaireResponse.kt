/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data.entity

import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire

@Serializable
data class QuestionnaireResponse(
	val questionnaires: List<Questionnaire?>? = null
)
