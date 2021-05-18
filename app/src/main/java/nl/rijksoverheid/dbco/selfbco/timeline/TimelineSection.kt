/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.timeline

import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.input.ContactInputItem
import nl.rijksoverheid.dbco.items.ui.DuoHeaderItem
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import org.joda.time.LocalDate

class TimelineSection(
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
        // Todo: Move to string resources without requiring context
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
            date.isEqual(today) -> "Vandaag ($formattedDate)"
            date.isEqual(today.minusDays(1)) -> "Gisteren ($formattedDate)"
            date.isEqual(today.minusDays(2)) -> "Eergisteren ($formattedDate)"
            else -> {
                "" + date.toString(DateFormats.selfBcoDateCheck).capitalize()
            }
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
            SelfBcoConstants.SYMPTOM_CHECK_FLOW -> "De eerste dag dat je klachten had"
            SelfBcoConstants.COVID_CHECK_FLOW -> "Op deze dag liet je jezelf testen"
            else -> null
        } else if (
            flowType == SelfBcoConstants.SYMPTOM_CHECK_FLOW &&
            date.isBefore(startDate)
        ) {
            "Deze dag was je mogelijk al besmettelijk"
        } else {
            null
        }
    }
}