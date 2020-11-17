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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemQuestionMultipleOptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import timber.log.Timber

class QuestionMultipleOptionsItem(
        val context: Context,
        question: Question?,
        val answerSelectedListener: (AnswerOption) -> Unit,
        private val previousAnswer: JsonObject? = null
) : BaseQuestionItem<ItemQuestionMultipleOptionsBinding>(question) {

    override fun getLayout() = R.layout.item_question_multiple_options
    private var selectedAnswer: AnswerOption? = null

    override fun bind(viewBinding: ItemQuestionMultipleOptionsBinding, position: Int) {
        viewBinding.item = this

        val list =
                question?.answerOptions?.map { option -> option?.label } ?: mutableListOf<String>()

        viewBinding.inputLayout.hint = question?.label
        val adapter: ArrayAdapter<String> = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                list.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        fillInPreviousAnswer(viewBinding)
        viewBinding.optionsSpinner.apply {
            this.adapter = adapter
            setSelection(0, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        p3: Long
                ) {
                    question?.answerOptions?.getOrNull(position)?.let {
                        answerSelectedListener.invoke(it)
                        selectedAnswer = it
                        viewBinding.inputEditText.setText(it.label)
                        Timber.d("Selected option $it")
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    selectedAnswer = null
                    viewBinding.inputEditText.setText("")
                }
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
            other is QuestionMultipleOptionsItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
            other is QuestionMultipleOptionsItem && other.question?.uuid == question?.uuid

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        selectedAnswer?.let {
            it.value?.let {
                answers.put("value", it)
            }
        }
        return answers
    }

    private fun fillInPreviousAnswer(viewBinding: ItemQuestionMultipleOptionsBinding) {
        previousAnswer?.let {
            val previousAnswerLabel = it["value"]?.jsonPrimitive?.jsonPrimitive?.content
            question?.answerOptions?.forEachIndexed { index, option ->
                if (option?.label?.equals(previousAnswerLabel) == true) {
                    answerSelectedListener.invoke(option)
                    selectedAnswer = option
                    viewBinding.inputEditText.setText(previousAnswerLabel)
                    viewBinding.optionsSpinner.setSelection(index, false)
                    return
                }
            }
            viewBinding.optionsSpinner.setSelection(0, false) // nothing was found
        }
    }
}
