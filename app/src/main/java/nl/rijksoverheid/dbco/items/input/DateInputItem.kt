/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.DatePicker
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.DateFormats.dateInputUI
import nl.rijksoverheid.dbco.databinding.ItemQuestionDateBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import org.joda.time.LocalDate


class DateInputItem(
    val context: Context,
    question: Question?
) :
    BaseQuestionItem<ItemQuestionDateBinding>(question), DatePickerDialog.OnDateSetListener {

    private var binding: ItemQuestionDateBinding? = null
    private var date: LocalDate? = null

    override fun getLayout() = R.layout.item_question_date

    override fun bind(viewBinding: ItemQuestionDateBinding, position: Int) {
        this.binding = viewBinding
        viewBinding.item = this

        date?.let {
            viewBinding.dateLabel.setText(it.toString(dateInputUI))
        }
    }

    fun onDateClicked(view: View) {
        val now = LocalDate.now()
        val dialog = DatePickerDialog(
            context,
            this,
            date?.year ?: now.year,
            date?.monthOfYear ?: now.monthOfYear,
            date?.dayOfMonth ?: now.dayOfMonth
        ) // default date 1 Jan 1980
        dialog.show()
    }

    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = LocalDate(year, month, dayOfMonth).apply {
            binding?.dateLabel?.setText(this.toString(dateInputUI))
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is DateInputItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is DateInputItem && other.question?.uuid == question?.uuid

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        date?.let {
            answers.put("value", it.toString(DateFormats.dateInputData))
        }
        return answers
    }
}
