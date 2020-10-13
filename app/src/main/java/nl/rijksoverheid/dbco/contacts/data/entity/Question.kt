/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
	val relevantForCategories: List<String?>? = null,
	val description: String? = null,
	val id: String? = null,
	val label: String? = null,
	val questionType: QuestionType? = null,
	val group: Group? = null,
	val answerOptions: List<AnswerOption?>? = null
)

@Serializable
enum class QuestionType {
    @SerialName("date")
    Date,

    @SerialName("classificationdetails")
    ClassificationDetails,

    @SerialName("open")
    Open,

    @SerialName("multiplechoice")
    Multiplechoice
}

@Serializable
enum class Group {
    @SerialName("contactdetails")
    ContactDetails,

    @SerialName("context")
    Context
}