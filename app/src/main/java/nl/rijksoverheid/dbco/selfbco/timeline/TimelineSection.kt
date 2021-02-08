/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.timeline

import android.widget.Toast
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.items.input.ContactInputItem
import nl.rijksoverheid.dbco.items.ui.ContactAddItem
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.items.ui.TimelineContactAddItem
import org.joda.time.DateTime
import timber.log.Timber

class TimelineSection(val date: DateTime, private val contactNames: Array<String>) : Section() {

    // Track items added per section, easier than parsing section adapter manually for now
    val items = ArrayList<ContactInputItem>()

    init {
        setSectionHeader(date)
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


    private fun setSectionHeader(date: DateTime) {
        Timber.d("Got date $date , comparing")
        // To do: Move to string resources without requiring context
        when {
            date.isEqual(DateTime.now().withTimeAtStartOfDay()) -> {
                setHeader(
                    SubHeaderItem(
                        String.format(
                            "Vandaag (%s)",
                            date.toString(DateFormats.selfBcoDateCheck)
                        )
                    )
                )
            }
            date.isEqual(DateTime.now().minusDays(1).withTimeAtStartOfDay()) -> {
                setHeader(
                    SubHeaderItem(
                        String.format(
                            "Gisteren (%s)",
                            date.toString(DateFormats.selfBcoDateCheck)
                        )
                    )
                )
            }
            date.isEqual(DateTime.now().minusDays(2).withTimeAtStartOfDay()) -> {
                setHeader(
                    SubHeaderItem(
                        String.format(
                            "Eergisteren (%s)",
                            date.toString(DateFormats.selfBcoDateCheck)
                        )
                    )
                )
            }
            else -> {
                setHeader(SubHeaderItem("" + date.toString(DateFormats.selfBcoDateCheck)))
            }
        }
    }


}