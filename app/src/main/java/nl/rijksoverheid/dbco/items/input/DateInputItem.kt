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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.DateFormats.dateInputUI
import nl.rijksoverheid.dbco.databinding.ItemQuestionDateBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import org.joda.time.LocalDate

class DateInputItem(
    val context: Context,
    question: Question?,
    private val previousAnswerValue: JsonObject? = null,
    private val isEnabled: Boolean
) :
    BaseQuestionItem<ItemQuestionDateBinding>(question), DatePickerDialog.OnDateSetListener {

    private var binding: ItemQuestionDateBinding? = null
    private var date: LocalDate? = null

    private val datePickerListener = { view: View ->
        showDatePicker()
    }

    override fun getLayout() = R.layout.item_question_date

    override fun bind(viewBinding: ItemQuestionDateBinding, position: Int) {
        this.binding = viewBinding
        viewBinding.item = this

        viewBinding.inputLabel.setOnClickListener(datePickerListener)

        if (date == null) {
            fillInPreviousAnswer()
        }

        date?.let {
            viewBinding.inputLabel.setText(it.toString(dateInputUI))
        }

        viewBinding.inputLayout.isEnabled = isEnabled
    }

    private fun showDatePicker() {
        // Default date is 1 January 1980
        val year = date?.year ?: 1980
        val month = (date?.monthOfYear ?: 1) - 1 // Note: LocalDate uses 1-12 for dates, DatePickerDialog's date uses 0-11 instead. Decrease date by one here
        val day = date?.dayOfMonth ?: 1

        val dialog = DatePickerDialog(
            context,
            R.style.SpinnerDatePickerDialogTheme,
            this,
            year,
            month,
            day
        )
        dialog.datePicker.calendarViewShown = false
        dialog.datePicker.spinnersShown = true
        dialog.show()

        // Override button color manually since Google doesn't support Spinner mode and/or spinner theming out of the box since API 24 & the Material design guidelines
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(context.resources.getColor(R.color.primary))
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(context.resources.getColor(R.color.primary))
    }

    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Same issue as before, LocalDate uses 1-12 for months instead of the 0-11 we get from the DatePickerDialog, increase date by 1 here for proper processing.
        date = LocalDate(year, month + 1, dayOfMonth).apply {
            binding?.inputLabel?.setText(this.toString(dateInputUI))
        }
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

    private fun fillInPreviousAnswer() {
        previousAnswerValue?.let { prevAnswer ->
            prevAnswer["value"]?.jsonPrimitive?.content?.let { value ->
                date = LocalDate.parse(value, DateFormats.dateInputData)
            }
        }
    }
}
