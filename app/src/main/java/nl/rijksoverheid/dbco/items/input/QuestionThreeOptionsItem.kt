/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.core.view.isVisible
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonObject
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestion2OptionsBinding
import nl.rijksoverheid.dbco.databinding.ItemQuestion3OptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.HtmlHelper

// TODO: refactor to a more generic solution supporting multiple options without dropdown
class QuestionThreeOptionsItem(
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswerValue: JsonObject? = null,
    private val isLocked: Boolean = false,
    private val isEnabled: Boolean
) : BaseOptionsQuestionItem<ItemQuestion3OptionsBinding>(
    context,
    question,
    answerSelectedListener,
    previousAnswerValue
) {

    override fun getLayout() = R.layout.item_question_3_options
    private lateinit var answerGroup: RadioGroup

    override fun bind(viewBinding: ItemQuestion3OptionsBinding, position: Int) {
        viewBinding.item = this
        answerGroup = viewBinding.answerGroup

        viewBinding.option1.setOnCheckedChangeListener(null)
        viewBinding.option2.setOnCheckedChangeListener(null)
        viewBinding.option3.setOnCheckedChangeListener(null)

        // try restore state
        if (selectedAnswer == null) {
            fillInPreviousAnswer()
        }

        // if there is no previous answer - reset clear selection
        if (selectedAnswer == null) {
            viewBinding.answerGroup.clearCheck()
        }

        question?.answerOptions?.indexOf(selectedAnswer)?.let { index ->
            when (index) {
                0 -> viewBinding.option1.isChecked = true
                1 -> viewBinding.option2.isChecked = true
                2 -> viewBinding.option3.isChecked = true
            }
        }

        val onCheckedChangeListener =
            CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    val answerOption = when (compoundButton.id) {
                        R.id.option1 -> question?.answerOptions?.get(0)
                        R.id.option2 -> question?.answerOptions?.get(1)
                        else -> question?.answerOptions?.get(2)
                    }
                    answerOption?.let {
                        selectedAnswer = it
                        answerSelectedListener.invoke(it)
                    }
                }
            }
        viewBinding.option1.setOnCheckedChangeListener(onCheckedChangeListener)
        viewBinding.option2.setOnCheckedChangeListener(onCheckedChangeListener)
        viewBinding.option3.setOnCheckedChangeListener(onCheckedChangeListener)

        question?.description?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.questionDescription.text = spannableBuilder
        }

        // If the input it locked due to the combination of task source and risk, disable the buttons but show the selection based on GGD input
        if (isLocked) {
            viewBinding.answerGroup.isEnabled = false
            viewBinding.option1.isEnabled = false
            viewBinding.option2.isEnabled = false
            viewBinding.option3.isEnabled = false
            viewBinding.option1.setOnCheckedChangeListener(null)
            viewBinding.option2.setOnCheckedChangeListener(null)
            viewBinding.option3.setOnCheckedChangeListener(null)

            viewBinding.questionLockedDescription.isVisible = true

            when {
                viewBinding.option1.isChecked -> {
                    viewBinding.option2.isVisible = false
                    viewBinding.option3.isVisible = false
                    viewBinding.option1.isVisible = true
                }
                viewBinding.option2.isChecked -> {
                    viewBinding.option1.isVisible = false
                    viewBinding.option3.isVisible = false
                    viewBinding.option2.isVisible = true
                }
                else -> {
                    viewBinding.option1.isVisible = false
                    viewBinding.option3.isVisible = true
                    viewBinding.option2.isVisible = false
                }
            }

        } else {
            viewBinding.answerGroup.isEnabled = isEnabled
            viewBinding.option1.isEnabled = isEnabled
            viewBinding.option2.isEnabled = isEnabled
            viewBinding.option3.isEnabled = isEnabled
            viewBinding.option1.isVisible = true
            viewBinding.option2.isVisible = true
            viewBinding.option3.isVisible = true
            viewBinding.questionLockedDescription.isVisible = false
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is QuestionThreeOptionsItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is QuestionThreeOptionsItem && other.question?.uuid == question?.uuid
}
