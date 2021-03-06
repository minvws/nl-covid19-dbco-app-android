/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.content.Context
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.core.view.isVisible
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestion2OptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.HtmlHelper

class QuestionTwoOptionsItem(
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswerValue: String? = null,
    private val isLocked: Boolean = false,
    private val isEnabled: Boolean,
    private val canShowEmptyWarning: Boolean = false,
) : BaseOptionsQuestionItem<ItemQuestion2OptionsBinding>(
    context,
    question,
    answerSelectedListener,
    previousAnswerValue
) {

    override fun getLayout() = R.layout.item_question_2_options
    private lateinit var answerGroup: RadioGroup

    override fun bind(viewBinding: ItemQuestion2OptionsBinding, position: Int) {
        viewBinding.item = this
        answerGroup = viewBinding.answerGroup
        viewBinding.option1.setOnCheckedChangeListener(null)
        viewBinding.option2.setOnCheckedChangeListener(null)

        // if there is no previous answer - reset clear selection
        if (selectedAnswer == null) {
            viewBinding.answerGroup.clearCheck()
            viewBinding.questionWarning.isVisible = canShowEmptyWarning
        }

        question?.answerOptions?.indexOf(selectedAnswer)?.let { index ->
            when (index) {
                0 -> viewBinding.option1.isChecked = true
                1 -> viewBinding.option2.isChecked = true
            }
        }

        CompoundButton.OnCheckedChangeListener { button, isChecked ->
            onCheckedChanged(isChecked, button, viewBinding)
        }.apply {
            viewBinding.option1.setOnCheckedChangeListener(this)
            viewBinding.option2.setOnCheckedChangeListener(this)
        }

        question?.description?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.questionDescription.text = spannableBuilder
        }

        // If the input it locked due to the combination of task source and risk, disable the buttons but show the selection based on GGD input
        checkLocked(viewBinding)
    }

    private fun checkLocked(viewBinding: ItemQuestion2OptionsBinding) {
        if (isLocked) {
            viewBinding.answerGroup.isEnabled = false
            viewBinding.option1.isEnabled = false
            viewBinding.option2.isEnabled = false
            viewBinding.option1.setOnCheckedChangeListener(null)
            viewBinding.option2.setOnCheckedChangeListener(null)
            viewBinding.questionLockedDescription.isVisible = true
            if (viewBinding.option1.isChecked) {
                viewBinding.option2.isVisible = false
                viewBinding.option1.isVisible = true
            } else {
                viewBinding.option1.isVisible = false
                viewBinding.option2.isVisible = true
            }
        } else {
            viewBinding.answerGroup.isEnabled = isEnabled
            viewBinding.option1.isEnabled = isEnabled
            viewBinding.option2.isEnabled = isEnabled
            viewBinding.option1.isVisible = true
            viewBinding.option2.isVisible = true
            viewBinding.questionLockedDescription.isVisible = false
        }
    }

    private fun onCheckedChanged(
        checked: Boolean,
        button: CompoundButton,
        viewBinding: ItemQuestion2OptionsBinding
    ) {
        if (checked) {
            val answerOption = when (button.id) {
                R.id.option1 -> question?.answerOptions?.get(0)
                else -> question?.answerOptions?.get(1)
            }
            answerOption?.let {
                selectedAnswer = it
                answerSelectedListener.invoke(it)
                viewBinding.questionWarning.isVisible = false
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is QuestionTwoOptionsItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is QuestionTwoOptionsItem && other.question?.uuid == question?.uuid
}
