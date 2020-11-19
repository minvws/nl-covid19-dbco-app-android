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
    context: Context,
    question: Question?,
    answerSelectedListener: (AnswerOption) -> Unit,
    previousAnswer: JsonObject? = null
) : BaseOptionsQuestionItem<ItemQuestionMultipleOptionsBinding>(context, question, answerSelectedListener, previousAnswer) {

    override fun getLayout() = R.layout.item_question_multiple_options

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

        if (selectedAnswer == null) {
            fillInPreviousAnswer()
        }

        viewBinding.inputEditText.setText(selectedAnswer?.label?:"")

        viewBinding.optionsSpinner.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                var check = 0
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (++check > 1) { // prevents spinner from firing during initialization
                        question?.answerOptions?.getOrNull(position)?.let {
                            answerSelectedListener.invoke(it)
                            selectedAnswer = it
                            viewBinding.inputEditText.setText(it.label)
                            Timber.d("Selected option $it")
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    selectedAnswer = null
                    viewBinding.inputEditText.setText("")
                }
            }
        }
    }
}
