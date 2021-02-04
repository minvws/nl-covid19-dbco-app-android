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
import android.widget.ArrayAdapter
import kotlinx.serialization.json.JsonObject
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestionMultipleOptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question

class QuestionMultipleOptionsItem(
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswer: JsonObject? = null,
    private val isLocked : Boolean = false,
    val isHidden : Boolean = false,
) : BaseOptionsQuestionItem<ItemQuestionMultipleOptionsBinding>(context, question, answerSelectedListener, previousAnswer) {

    override fun getLayout() = R.layout.item_question_multiple_options

    override fun bind(viewBinding: ItemQuestionMultipleOptionsBinding, position: Int) {
        viewBinding.item = this

        viewBinding.inputLayout.hint = question?.label

        // Populate adapter with the answer options
        val labels = question?.answerOptions?.map { it?.label }.orEmpty()
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            context,
            R.layout.item_dropdown,
            labels
        )
        viewBinding.inputEditText.setAdapter(adapter) // Dropdown is shown on click

        // Listen to selections that happen in the dropdown
        viewBinding.inputEditText.setOnItemClickListener { _, _, position, _ ->
            question?.answerOptions?.getOrNull(position)?.let { answer ->
                answerSelectedListener.invoke(answer)
                selectedAnswer = answer
            }
        }

        viewBinding.inputEditText.setText(selectedAnswer?.label ?: "")

        if (selectedAnswer == null) {
            fillInPreviousAnswer()
        }

        // If values are set through the portal this item should be locked from input
        if (isLocked){
            viewBinding.inputLayout.isEnabled = false
            viewBinding.inputEditText.isEnabled = false
            viewBinding.questionLockedDescription.visibility = View.VISIBLE
        } else {
            viewBinding.inputLayout.isEnabled = true
            viewBinding.inputEditText.isEnabled = true
            viewBinding.questionLockedDescription.visibility = View.GONE
        }
    }
}
