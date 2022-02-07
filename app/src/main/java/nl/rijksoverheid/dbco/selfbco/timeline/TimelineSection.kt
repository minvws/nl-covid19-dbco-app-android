/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.timeline

import android.content.Context
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.input.ContactInputItem
import nl.rijksoverheid.dbco.items.ui.DuoHeaderItem
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants.Companion.COVID_CHECK_FLOW
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants.Companion.SYMPTOM_CHECK_FLOW
import org.joda.time.LocalDate
import java.util.*

class TimelineSection(
    private val context: Context,
    val date: LocalDate,
    private val contactNames: Array<String>,
    startDate: LocalDate,
    private val flowType: Int,
    private val deleteListener: (String?) -> Unit
) : Section() {

    init {
        setSectionHeader(startDate, date)
        setFooter(
            TimelineContactAddItem(
                this,
                object : TimelineContactAddItem.OnAddClickedListener {
                    override fun onAddClicked(section: TimelineSection) {
                        // Add new item, add trashcan listener like we did with roommates.
                        // Same principle only on a per section base
                        addContactToTimeline()
                    }
                })
        )
    }

    fun addContactToTimeline(
        name: String = "",
        uuid: String? = null,
        focusOnBind: Boolean = true
    ): ContactInputItem {
        val item = ContactInputItem(
            contactName = name,
            contactUuid = uuid,
            contentDescriptionSuffix = context.getString(
                R.string.add_contact_edit_field_contentDescription_date_suffix,
                date.toString(DateFormats.selfBcoDateOnly)
            ),
            focusOnBind = focusOnBind,
            contactNames = contactNames,
            trashListener = object : ContactInputItem.OnTrashClickedListener {
                override fun onTrashClicked(item: ContactInputItem) {
                    this@TimelineSection.remove(item)
                    deleteListener(item.contactUuid)
                }
            })
        add(item)
        return item
    }

    fun refreshHeader(newStartDate: LocalDate) {
        removeHeader()
        setSectionHeader(newStartDate, date)
    }

    fun getContactItems(): List<ContactInputItem> {
        val items = mutableListOf<ContactInputItem>()
        for (groupIndex: Int in 0 until groupCount) {
            val item = getItem(groupIndex)
            if (item is ContactInputItem) {
                items.add(item)
            }
        }
        return items
    }

    private fun setSectionHeader(startDate: LocalDate, date: LocalDate) {
        setHeader(
            createHeader(
                flowType = flowType,
                today = LocalDate.now(),
                date = date,
                startDate = startDate
            )
        )
    }

    private fun createHeader(
        flowType: Int,
        today: LocalDate,
        date: LocalDate,
        startDate: LocalDate
    ): BaseBindableItem<*> {

        val subtitle = getSubtitle(flowType, date, startDate)
        val formattedDate = date.toString(DateFormats.selfBcoDateCheck)
        val title = when {
            date.isEqual(today) -> context.getString(
                R.string.selfbco_timeline_today_date,
                formattedDate
            )
            date.isEqual(today.minusDays(1)) -> context.getString(
                R.string.selfbco_timeline_yesterday_date,
                formattedDate
            )
            date.isEqual(today.minusDays(2)) -> context.getString(
                R.string.selfbco_timeline_day_before_yesterday_date,
                formattedDate
            )
            else -> date.toString(DateFormats.selfBcoDateCheck).capitalize(Locale.getDefault())
        }

        return if (subtitle != null) {
            DuoHeaderItem(title, subtitle)
        } else {
            SubHeaderItem(title)
        }
    }

    private fun getSubtitle(
        flowType: Int,
        date: LocalDate,
        startDate: LocalDate
    ): String? {
        return if (date == startDate) when (flowType) {
            SYMPTOM_CHECK_FLOW -> context.getString(R.string.selfbco_timeline_first_day_of_symptoms)
            COVID_CHECK_FLOW -> context.getString(R.string.selfbco_timeline_day_of_test)
            else -> null
        } else if (flowType == SYMPTOM_CHECK_FLOW && date.isBefore(startDate)) {
            context.getString(R.string.selfbco_timeline_first_contagious_date)
        } else {
            null
        }
    }
}