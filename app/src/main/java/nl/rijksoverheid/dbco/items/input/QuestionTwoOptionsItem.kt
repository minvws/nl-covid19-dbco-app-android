/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestion2OptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.HtmlHelper

class QuestionTwoOptionsItem(
    question: Question?,
    private val answerSelectedListener: (AnswerOption) -> Unit,
    private val previousAnswer: JsonObject? = null
) : BaseQuestionItem<ItemQuestion2OptionsBinding>(question) {

    override fun getLayout() = R.layout.item_question_2_options
    private var selectedAnswer: AnswerOption? = null
    private lateinit var answerGroup: RadioGroup

    override fun bind(viewBinding: ItemQuestion2OptionsBinding, position: Int) {
        viewBinding.item = this
        answerGroup = viewBinding.answerGroup

        viewBinding.option1.setOnCheckedChangeListener(null)
        viewBinding.option2.setOnCheckedChangeListener(null)

        // try restore state
        if (selectedAnswer == null) {
            fillInPreviousAnswer()
        }

        // if there is no previous answer - reset clear selection
        if (selectedAnswer == null) {
            viewBinding.option1.isChecked = false
            viewBinding.option2.isChecked = false
        }

        question?.answerOptions?.indexOf(selectedAnswer)?.let {index ->
            when (index) {
                0 -> viewBinding.option1.isChecked = true
                1 -> viewBinding.option2.isChecked = true
            }
        }

        val onCheckedChangeListener =
            CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    val answerOption = when (compoundButton.id) {
                        R.id.option1 -> question?.answerOptions?.get(0)
                        else -> question?.answerOptions?.get(1)
                    }
                    answerOption?.let {
                        selectedAnswer = it
                        answerSelectedListener.invoke(it)
                    }
                }
            }
        viewBinding.option1.setOnCheckedChangeListener(onCheckedChangeListener)
        viewBinding.option2.setOnCheckedChangeListener(onCheckedChangeListener)

        question?.description?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.questionDescription.text = spannableBuilder
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is QuestionTwoOptionsItem && other.question?.uuid == question?.uuid && other.question?.label == question?.label

    override fun hasSameContentAs(other: Item<*>) =
        other is QuestionTwoOptionsItem && other.question?.uuid == question?.uuid && other.question?.label == question?.label

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        selectedAnswer?.let {
            it.value?.let {value ->
                answers.put("value", value)
            }
        }
        return answers
    }

    private fun fillInPreviousAnswer() {
        previousAnswer?.let { prevAnswer ->
            prevAnswer["value"]?.let { value ->
                when (value.jsonPrimitive.jsonPrimitive.content) {
                    question?.answerOptions?.get(0)?.value -> {
                        selectedAnswer = question.answerOptions[0]
                    }
                    question?.answerOptions?.get(1)?.value -> {
                        selectedAnswer = question.answerOptions[1]
                    }
                    else -> {
                    }
                }
            } ?: run {
                prevAnswer["trigger"]?.let { trigger ->
                    question?.answerOptions?.forEach { option ->
                        if (option?.trigger == trigger.jsonPrimitive.jsonPrimitive.content) {
                            selectedAnswer = option
                        }
                    }
                }
            }

        }
    }
}
