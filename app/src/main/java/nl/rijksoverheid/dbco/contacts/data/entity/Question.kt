/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Question(
    val relevantForCategories: List<Category?>? = null,
    val description: String? = null,
    val uuid: String? = null,
    val label: String? = null,
    val questionType: QuestionType? = null,
    val group: Group? = null,
    val answerOptions: List<AnswerOption?>? = null
)

@Keep
@Serializable
enum class QuestionType {

    @SerialName("date")
    Date,

    @SerialName("classificationdetails")
    ClassificationDetails,

    @SerialName("contactdetails")
    ContactDetails,

    @SerialName("open")
    Open,

    @SerialName("multiplechoice")
    Multiplechoice
}

@Keep
@Serializable
enum class Group {

    @SerialName("classification")
    Classification,

    @SerialName("contactdetails")
    ContactDetails,

    @SerialName("context")
    Context
}
