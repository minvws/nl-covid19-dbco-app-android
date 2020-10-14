/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.DatePicker
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.entity.Question
import nl.rijksoverheid.dbco.databinding.ItemQuestionDateBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


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
            viewBinding.dateLabel.text = it.toString(FORMAT)
        }
    }

    fun onDateClicked(view: View) {
        val dialog = DatePickerDialog(
            context,
            this,
            date?.year ?: 1980,
            date?.monthOfYear ?: 1,
            date?.dayOfMonth ?: 1
        ) // default date 1 Jan 1980
        dialog.show()
    }

    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = LocalDate(year, month, dayOfMonth).apply {
            binding?.dateLabel?.text = this.toString(FORMAT)
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is DateInputItem && other.question?.id == question?.id

    override fun hasSameContentAs(other: Item<*>) =
        other is DateInputItem && other.question?.id == question?.id

    companion object {
        val FORMAT: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy")
    }
}
