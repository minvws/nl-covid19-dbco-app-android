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
import timber.log.Timber

class QuestionTwoOptionsItem(
        question: Question?,
        private val answerSelectedListener: (AnswerOption) -> Unit,
        private val optionalValueLabel: String? = null,
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
        fillInPreviousAnswer()

        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                val answerOption = when (compoundButton.id) {
                    R.id.option1 -> question?.answerOptions?.get(0)
                    else -> question?.answerOptions?.get(1)
                }
                answerOption?.let {
                    answerSelectedListener.invoke(it)
                    selectedAnswer = it
                    checkCompleted()
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

    override fun isRequired(): Boolean = true

    override fun isCompleted(): Boolean {
        return (selectedAnswer != null)
    }

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        selectedAnswer?.let {
            it.value?.let {
                if (optionalValueLabel != null) {
                    answers.put(optionalValueLabel, it)
                } else {
                    answers.put("value", it)
                }
            }
        }
        return answers
    }

    private fun fillInPreviousAnswer() {
        if (previousAnswer != null && optionalValueLabel != null && previousAnswer.containsKey(optionalValueLabel)
        ) {
            val previousAnswerValue = previousAnswer[optionalValueLabel]?.jsonPrimitive?.content
            Timber.d("Found previous value for $optionalValueLabel of $previousAnswerValue")

            if (question?.answerOptions?.get(0)?.value == previousAnswerValue) {
                (answerGroup.getChildAt(0) as RadioButton).isChecked = true
                question?.answerOptions?.get(0)?.let {
                    selectedAnswer = it
                }
            } else {
                (answerGroup.getChildAt(1) as RadioButton).isChecked = true
                question?.answerOptions?.get(1)?.let {
                    selectedAnswer = it
                }
            }

        }
    }
}
