/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.ui

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.entity.AnswerOption
import nl.rijksoverheid.dbco.contacts.data.entity.Question
import nl.rijksoverheid.dbco.databinding.ItemQuestionMultipleOptionsBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.QuestionnaireItem
import nl.rijksoverheid.dbco.items.QuestionnaireItemViewState
import nl.rijksoverheid.dbco.util.SpinnerAdapterWithDefaultLabel
import timber.log.Timber


class QuestionMultipleOptionsItem(
    val context: Context,
    val question: Question?,
    val answerSelectedListener: (AnswerOption) -> Unit
) :
    BaseBindableItem<ItemQuestionMultipleOptionsBinding>(), QuestionnaireItem {

    override fun getLayout() = R.layout.item_question_multiple_options
    private var selectedAnswer: AnswerOption? = null
    private val currentViewState: MutableLiveData<QuestionnaireItemViewState> = MutableLiveData()

    init {
        currentViewState.value = QuestionnaireItemViewState()
    }

    override fun bind(viewBinding: ItemQuestionMultipleOptionsBinding, position: Int) {
        viewBinding.item = this

        val list =
            question?.answerOptions?.map { option -> option?.label } ?: mutableListOf<String>()

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
                        selectedAnswer = it
                        Timber.d("Selected option $it")
                        currentViewState.value = currentViewState.value!!.copy(isCompleted = true)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    selectedAnswer = null
                    currentViewState.value = currentViewState.value!!.copy(isCompleted = false)
                }
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is QuestionMultipleOptionsItem && other.question?.id == question?.id

    override fun hasSameContentAs(other: Item<*>) =
        other is QuestionMultipleOptionsItem && other.question?.id == question?.id

    override fun isRequired(): Boolean = true

    override fun getItemType(): ItemType = ItemType.INPUT_MULTIPLE_CHOICE

    override fun isCompleted(): Boolean {
        return (selectedAnswer != null)
    }

    override fun getViewStateLiveData(): MutableLiveData<QuestionnaireItemViewState> {
        return currentViewState
    }


}
