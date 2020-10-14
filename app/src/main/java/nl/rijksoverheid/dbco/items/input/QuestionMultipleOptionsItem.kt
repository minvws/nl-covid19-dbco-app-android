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
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.entity.AnswerOption
import nl.rijksoverheid.dbco.contacts.data.entity.Question
import nl.rijksoverheid.dbco.databinding.ItemQuestionMultipleOptionsBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.SpinnerAdapterWithDefaultLabel


class QuestionMultipleOptionsItem(
    val context: Context,
    question: Question?,
    val answerSelectedListener: (AnswerOption) -> Unit
) :
    BaseQuestionItem<ItemQuestionMultipleOptionsBinding>(question) {

    override fun getLayout() = R.layout.item_question_multiple_options

    override fun bind(viewBinding: ItemQuestionMultipleOptionsBinding, position: Int) {
        viewBinding.item = this

        val list = question?.answerOptions?.map { option -> option?.label } ?: mutableListOf<String>()

        val adapter: ArrayAdapter<String> = SpinnerAdapterWithDefaultLabel(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                list.toTypedArray(),
                context.getString(R.string.contact_question_select_option)
            )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.optionsSpinner.apply {
            this.adapter = adapter
            prompt = context.getString(R.string.contact_question_select_option)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    question?.answerOptions?.getOrNull(position)?.let {
                        answerSelectedListener.invoke(it)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is QuestionMultipleOptionsItem && other.question?.id == question?.id

    override fun hasSameContentAs(other: Item<*>) =
        other is QuestionMultipleOptionsItem && other.question?.id == question?.id
}
