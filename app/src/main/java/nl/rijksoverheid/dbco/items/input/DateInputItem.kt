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
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.DateFormats.dateInputUI
import nl.rijksoverheid.dbco.databinding.ItemQuestionDateBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import kotlin.collections.HashMap

class DateInputItem(
    val context: Context,
    question: Question?,
    private val answerSelectedListener: (String) -> Unit,
    private val previousAnswerValue: String? = null,
    private val isEnabled: Boolean
) :
    BaseQuestionItem<ItemQuestionDateBinding>(question) {

    private var binding: ItemQuestionDateBinding? = null

    private var date: LocalDate? = getPreviousAnswer()

    private val datePickerListener = { _: View ->
        showDatePicker()
    }

    override fun getLayout() = R.layout.item_question_date

    override fun bind(viewBinding: ItemQuestionDateBinding, position: Int) {
        this.binding = viewBinding
        viewBinding.item = this

        viewBinding.inputLabel.setOnClickListener(datePickerListener)

        date?.let {
            viewBinding.inputLabel.setText(it.toString(dateInputUI))
        }

        viewBinding.inputLayout.isEnabled = isEnabled
    }

    private fun showDatePicker() {
        val current = date ?: LocalDate(1980, 1, 1) // default date is january 1st, 1980
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.selfbco_date_title)
            .setSelection(current.toDateTimeAtStartOfDay(DateTimeZone.UTC).millis)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    date = LocalDate(selection).also {
                        val text = it.toString(dateInputUI)
                        binding?.inputLabel?.setText(text)
                        answerSelectedListener(it.toString(DateFormats.dateInputData))
                    }
                }
            }.also { it.show((context as FragmentActivity).supportFragmentManager, "BirthdayPicker"); }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is DateInputItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is DateInputItem && other.question?.uuid == question?.uuid

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        date?.let {
            answers.put("value", it.toString(DateFormats.dateInputData).toJsonPrimitive())
        }
        return answers
    }

    private fun getPreviousAnswer(): LocalDate? {
        return previousAnswerValue?.let { prevAnswer ->
            LocalDate.parse(prevAnswer, DateFormats.dateInputData)
        }
    }
}
