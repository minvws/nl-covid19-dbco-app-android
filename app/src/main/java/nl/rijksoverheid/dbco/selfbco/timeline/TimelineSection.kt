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
import nl.rijksoverheid.dbco.items.ui.DuoHeaderItem
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.items.ui.TimelineContactAddItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import org.joda.time.DateTime
import timber.log.Timber

class TimelineSection(
    val date: DateTime,
    private val contactNames: Array<String>,
    private val dateOfSymptomOnset: DateTime,
    private val flowType: Int
) : Section() {

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
        // Todo: Move to string resources without requiring context
        when {
            // Today
            date.isEqual(DateTime.now().withTimeAtStartOfDay()) -> {
                if (date.isEqual(dateOfSymptomOnset)) {
                    when (flowType) {
                        // Todo: Find a better way to handle this, there's a lot of logic for a simple text
                        SelfBcoConstants.COVID_CHECK_FLOW -> {
                            setHeader(
                                SubHeaderItem(
                                    String.format(
                                        "Vandaag (%s)",
                                        date.toString(DateFormats.selfBcoDateCheck)
                                    )
                                )
                            )
                        }
                        SelfBcoConstants.SYMPTOM_CHECK_FLOW -> {
                            setHeader(
                                DuoHeaderItem(
                                    String.format(
                                        "Vandaag (%s)",
                                        date.toString(DateFormats.selfBcoDateCheck)
                                    ),
                                    "De eerste dag dat je klachten had"
                                )
                            )
                        }
                        SelfBcoConstants.NOT_SELECTED -> {
                            setHeader(
                                SubHeaderItem(
                                    String.format(
                                        "Vandaag (%s)",
                                        date.toString(DateFormats.selfBcoDateCheck)
                                    )
                                )
                            )
                        }
                    }
                } else {
                    setHeader(
                        SubHeaderItem(
                            String.format(
                                "Vandaag (%s)",
                                date.toString(DateFormats.selfBcoDateCheck)
                            )
                        )
                    )
                }
            }
            // Yesterday
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
            // Day before yesterday
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
            // Everything else
            else -> {
                setHeader(SubHeaderItem("" + date.toString(DateFormats.selfBcoDateCheck)))
            }
        }
    }


}