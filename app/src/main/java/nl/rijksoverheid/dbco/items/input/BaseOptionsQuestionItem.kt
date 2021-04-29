/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.content.Context
import androidx.databinding.ViewDataBinding
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive

abstract class BaseOptionsQuestionItem<T : ViewDataBinding>(
    val context: Context,
    question: Question?,
    val answerSelectedListener: (AnswerOption) -> Unit,
    private val previousAnswerValue: String? = null
) : BaseQuestionItem<T>(question) {

    var selectedAnswer: AnswerOption? = getPreviousAnswer()

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        selectedAnswer?.let {
            it.value?.let { value ->
                answers.put("value", value.toJsonPrimitive())
            }
        }
        return answers
    }

    private fun getPreviousAnswer(): AnswerOption? {
        previousAnswerValue?.let { prevAnswer ->
            question?.answerOptions?.forEach { option ->
                if (option?.value == prevAnswer) {
                    return option
                }
            }
        }
        return null
    }

    open fun clearPreviousAnswer() {
        selectedAnswer = null
    }
}
