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
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswer: JsonObject? = null
) : BaseOptionsQuestionItem<ItemQuestion2OptionsBinding>(context, question, answerSelectedListener, previousAnswer) {

    override fun getLayout() = R.layout.item_question_2_options
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
}
