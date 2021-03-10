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
import nl.rijksoverheid.dbco.items.ui.TimelineContactAddItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import org.joda.time.DateTime
import timber.log.Timber

class TimelineSection(
    val date: DateTime,
    private val contactNames: Array<String>,
    dateOfSymptomOnset: DateTime,
    private val flowType: Int
) : Section() {

    // Track items added per section, easier than parsing section adapter manually for now
    val items = ArrayList<ContactInputItem>()

    init {
        setSectionHeader(dateOfSymptomOnset, date)
        setFooter(
            TimelineContactAddItem(
                this,
                object : TimelineContactAddItem.OnAddClickedListener {
                    override fun onAddClicked(section: TimelineSection) {
                        // Add new item, add trashcan listener like we did with roommates.
                        // Same principle only on a per section base
                        val item =
                            ContactInputItem(
                                contactNames, "",
                                trashListener = object : ContactInputItem.OnTrashClickedListener {
                                    override fun onTrashClicked(item: ContactInputItem) {
                                        Timber.d("Clicked trash for item with text ${item.contactName}")
                                        this@TimelineSection.remove(item)
                                        items.remove(item)
                                    }
                                })
                        this@TimelineSection.add(item)
                        items.add(item)
                    }
                })
        )
    }

    fun refreshHeader(newSymptomOnsetDate: DateTime) {
        removeHeader()
        setSectionHeader(newSymptomOnsetDate, date)
    }


    private fun setSectionHeader(dateOfSymptomOnset: DateTime, date: DateTime) {
        Timber.d("Got date $date , comparing")
        // Todo: Move to string resources without requiring context
        setHeader(
            createHeader(
                flowType = flowType,
                today = DateTime.now(),
                date = date,
                dateOfSymptomOnset = dateOfSymptomOnset
            )
        )
    }

    private fun createHeader(
        flowType: Int,
        today: DateTime,
        date: DateTime,
        dateOfSymptomOnset: DateTime
    ): BaseBindableItem<*> {

        val subtitle = getSubtitle(flowType, date, dateOfSymptomOnset)
        val title = when {
            date.isEqual(today.withTimeAtStartOfDay()) -> {
                String.format(
                    "Vandaag (%s)",
                    date.toString(DateFormats.selfBcoDateCheck)
                )
            }
            date.isEqual(today.minusDays(1).withTimeAtStartOfDay()) -> {
                String.format(
                    "Gisteren (%s)",
                    date.toString(DateFormats.selfBcoDateCheck)
                )
            }
            date.isEqual(today.minusDays(2).withTimeAtStartOfDay()) -> {
                String.format(
                    "Eergisteren (%s)",
                    date.toString(DateFormats.selfBcoDateCheck)
                )
            }
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
        date: DateTime,
        dateOfSymptomOnset: DateTime
    ): String? {
        return if (date == dateOfSymptomOnset) when (flowType) {
            SelfBcoConstants.SYMPTOM_CHECK_FLOW -> "De eerste dag dat je klachten had"
            SelfBcoConstants.COVID_CHECK_FLOW -> "Op deze dag liet je jezelf testen"
            else -> null
        } else if (
            flowType == SelfBcoConstants.SYMPTOM_CHECK_FLOW &&
            date.isBefore(dateOfSymptomOnset)
        ) {
            "Deze dag was je mogelijk al besmettelijk"
        } else {
            null
        }
    }
}