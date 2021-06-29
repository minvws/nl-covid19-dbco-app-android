/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

/**
 * Possible date formats used throughout the app
 */
object DateFormats {
    val datePickerDate: DateTimeFormatter = DateTimeFormat.forPattern("EEEE, d MMMM").withLocale(Locale("nl"))
    val datePickerYear: DateTimeFormatter = DateTimeFormat.forPattern("yyyy")
    val datePicker: DateTimeFormatter = DateTimeFormat.forPattern("EEEE, d MMMM yyyy").withLocale(Locale("nl"))
    val dateInputData: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val questionData: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val pairingData: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val expiryData: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val informDate: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val exposureUI: DateTimeFormatter = DateTimeFormat.forPattern("EEEE dd MMMM yyyy").withLocale(Locale("nl"))
    val informContactGuidelinesUI: DateTimeFormatter = DateTimeFormat.forPattern("d MMMM").withLocale(Locale("nl"))
    val dateInputUI: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy").withLocale(Locale("nl"))
    val selfBcoDateCheck : DateTimeFormatter = DateTimeFormat.forPattern("EEEE d MMMM").withLocale(Locale("nl"))
    val selfBcoDateOnly : DateTimeFormatter = DateTimeFormat.forPattern("d MMMM").withLocale(Locale("nl"))
}