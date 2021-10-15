/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnaire.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.CategoryHolder

@Keep
@Serializable
data class Question(
    val description: String? = null,
    val label: String? = null,
    val explanation: String? = null,
    val questionType: QuestionType? = null,
    val group: Group? = null,
    val answerOptions: List<AnswerOption?>? = null
) {

    private val relevantForCategories: List<CategoryHolder?>? = null
    var uuid: String? = null

    fun isRelevantForCategory(category: Category?): Boolean {
        if (category == null || category == Category.NO_RISK) {
            return false
        }
        relevantForCategories?.forEach {
            if (it?.category == category) {
                return true
            }
        }
        return false
    }
}

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
