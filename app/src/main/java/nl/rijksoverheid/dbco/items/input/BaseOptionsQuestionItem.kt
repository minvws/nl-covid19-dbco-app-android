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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question

abstract class BaseOptionsQuestionItem<T : ViewDataBinding>(
    val context: Context,
    question: Question?,
    val answerSelectedListener: (AnswerOption) -> Unit,
    private val previousAnswer: JsonObject? = null
) : BaseQuestionItem<T>(question) {

    var selectedAnswer: AnswerOption? = null

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        selectedAnswer?.let {
            it.value?.let { value ->
                answers.put("value", value)
            }
        }
        return answers
    }

    open fun fillInPreviousAnswer() {
        previousAnswer?.let { prevAnswer ->
            prevAnswer["value"]?.jsonPrimitive?.jsonPrimitive?.content.let { value ->
                question?.answerOptions?.forEach { option ->
                    if (option?.value == value) {
                        selectedAnswer = option
                        return
                    }
                }
            }
            prevAnswer["trigger"]?.jsonPrimitive?.jsonPrimitive?.content.let { trigger ->
                question?.answerOptions?.forEach { option ->
                    if (option?.trigger == trigger) {
                        selectedAnswer = option
                    }
                }
            }
        }
    }
}
