/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.content.Context
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestionMultipleOptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question

class QuestionMultipleOptionsItem(
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswer: String? = null,
    private val isLocked: Boolean = false,
    val isEnabled: Boolean,
) : BaseOptionsQuestionItem<ItemQuestionMultipleOptionsBinding>(
    context,
    question,
    answerSelectedListener,
    previousAnswer
) {

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
        viewBinding.inputLabel.setAdapter(adapter) // Dropdown is shown when end icon is clicked

        viewBinding.inputLabel.setOnClickListener {
            if (viewBinding.inputLabel.text.isNotEmpty()) {
                adapter.filter.filter(null) // Do not filter to show all options at all time
            }
            viewBinding.inputLabel.showDropDown()
        }

        // Listen to selections that happen in the dropdown
        viewBinding.inputLabel.setOnItemClickListener { _, _, position, _ ->
            question?.answerOptions?.getOrNull(position)?.let { answer ->
                answerSelectedListener.invoke(answer)
                selectedAnswer = answer
            }
        }

        selectedAnswer?.let {
            viewBinding.inputLabel.setText(it.label)
        }
        viewBinding.inputLabel.setOnKeyListener(null)

        // If values are set through the portal this item should be locked from input
        if (isLocked) {
            viewBinding.inputLayout.isEnabled = false
            viewBinding.inputLabel.isEnabled = false
            viewBinding.questionLockedDescription.isVisible = false
        } else {
            viewBinding.inputLayout.isEnabled = isEnabled
            viewBinding.inputLabel.isEnabled = isEnabled
            viewBinding.questionLockedDescription.isVisible = false
        }
    }
}
