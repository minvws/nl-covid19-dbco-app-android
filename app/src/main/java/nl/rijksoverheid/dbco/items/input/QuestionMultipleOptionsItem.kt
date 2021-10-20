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
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import com.xwray.groupie.viewbinding.GroupieViewHolder
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestionMultipleOptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.setError

class QuestionMultipleOptionsItem(
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswer: String? = null,
    private val isLocked: Boolean = false,
    val isEnabled: Boolean,
    private val canShowEmptyWarning: Boolean = false
) : BaseOptionsQuestionItem<ItemQuestionMultipleOptionsBinding>(
    context,
    question,
    answerSelectedListener,
    previousAnswer
) {

    override fun getLayout() = R.layout.item_question_multiple_options

    override fun bind(viewBinding: ItemQuestionMultipleOptionsBinding, position: Int) {

        viewBinding.inputLayout.hint = question?.label

        val adapter = ArrayAdapter(
            context,
            R.layout.item_dropdown,
            question?.answerOptions?.map { it?.label }.orEmpty()
        )

        viewBinding.requireEditText().setAdapter(adapter)

        viewBinding.requireEditText().setOnClickListener {
            if (viewBinding.requireEditText().text.isNotEmpty()) {
                adapter.filter.filter(null)
            }
            viewBinding.requireEditText().showDropDown()
        }

        if (selectedAnswer == null && canShowEmptyWarning) {
            viewBinding.inputLayout.setError(
                R.drawable.ic_warning_24,
                R.string.warning_necessary,
                R.color.purple
            )
        } else {
            viewBinding.inputLayout.error = null
        }

        // Listen to selections that happen in the dropdown
        viewBinding.requireEditText().setOnItemClickListener { _, _, clickPosition, _ ->
            question?.answerOptions?.getOrNull(clickPosition)?.let { answer ->
                answerSelectedListener.invoke(answer)
                selectedAnswer = answer
                viewBinding.inputLayout.error = null
            }
        }

        viewBinding.requireEditText().setText(selectedAnswer?.label)
        viewBinding.requireEditText().setOnKeyListener(null)

        // If values are set through the portal this item should be locked from input
        if (isLocked) {
            viewBinding.inputLayout.isEnabled = false
            viewBinding.requireEditText().isEnabled = false
            viewBinding.questionLockedDescription.isVisible = false
        } else {
            viewBinding.inputLayout.isEnabled = isEnabled
            viewBinding.requireEditText().isEnabled = isEnabled
            viewBinding.questionLockedDescription.isVisible = false
        }

        viewBinding.questionExplanationContainer.isVisible = question?.explanation != null
        viewBinding.questionExplanation.text = question?.explanation
    }

    override fun unbind(viewHolder: GroupieViewHolder<ItemQuestionMultipleOptionsBinding>) {
        viewHolder.binding.requireEditText().setOnClickListener(null)
        viewHolder.binding.requireEditText().onItemClickListener = null
        super.unbind(viewHolder)
    }

    private fun ItemQuestionMultipleOptionsBinding.requireEditText(): AutoCompleteTextView =
        this.inputLayout.editText!! as AutoCompleteTextView
}
