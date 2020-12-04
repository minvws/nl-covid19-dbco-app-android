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
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonObject
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestion2OptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.HtmlHelper

class QuestionTwoOptionsItem(
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswerValue: JsonObject? = null,
    private val isLocked : Boolean = false
) : BaseOptionsQuestionItem<ItemQuestion2OptionsBinding>(context, question, answerSelectedListener, previousAnswerValue) {

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

        // If the input it locked due to the combination of task source and risk, disable the buttons but show the selection based on GGD input
        if(isLocked){
            viewBinding.answerGroup.isEnabled = false
            viewBinding.option1.isEnabled = false
            viewBinding.option2.isEnabled = false
            viewBinding.option1.setOnCheckedChangeListener(null)
            viewBinding.option2.setOnCheckedChangeListener(null)

            viewBinding.questionLockedDescription.visibility = View.VISIBLE

            if(viewBinding.option1.isChecked){
                viewBinding.option2.visibility = View.GONE
                viewBinding.option1.visibility = View.VISIBLE
            } else {
                viewBinding.option1.visibility = View.GONE
                viewBinding.option2.visibility = View.VISIBLE
            }

        } else {
            viewBinding.answerGroup.isEnabled = true
            viewBinding.option1.isEnabled = true
            viewBinding.option2.isEnabled = true
            viewBinding.option1.visibility = View.VISIBLE
            viewBinding.option2.visibility = View.VISIBLE
            viewBinding.questionLockedDescription.visibility = View.GONE

        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is QuestionTwoOptionsItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is PhoneNumberItem && other.question?.uuid == question?.uuid
}
