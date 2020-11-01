/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.widget.CompoundButton
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestion2OptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.HtmlHelper

class QuestionTwoOptionsItem(
    question: Question?,
    private val answerSelectedListener: (AnswerOption) -> Unit,
    private val optionalValueLabel: String? = null
) : BaseQuestionItem<ItemQuestion2OptionsBinding>(question) {

    override fun getLayout() = R.layout.item_question_2_options
    private var selectedAnswer: AnswerOption? = null

    override fun bind(viewBinding: ItemQuestion2OptionsBinding, position: Int) {
        viewBinding.item = this

        question?.description?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.questionDescription.text = spannableBuilder
        }
    }

    fun onCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            val answerOption = when (buttonView.id) {
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

    override fun isSameAs(other: Item<*>): Boolean =
        other is QuestionTwoOptionsItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is QuestionTwoOptionsItem && other.question?.uuid == question?.uuid

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
}
