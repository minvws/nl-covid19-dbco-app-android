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
 * Created by Dima Kovalenko.
 */
object DateFormats {
    val dateInputData: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val questionData: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val exposureUI: DateTimeFormatter = DateTimeFormat.forPattern("EEEE dd MMMM yyyy").withLocale(Locale("nl"))
    val informContactGuidelinesUI: DateTimeFormatter = DateTimeFormat.forPattern("d MMMM").withLocale(Locale("nl"))
    val dateInputUI: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy").withLocale(Locale("nl"))
    val selfBcoDateCheck : DateTimeFormatter = DateTimeFormat.forPattern("EEEE d MMMM").withLocale(Locale("nl"))
    val selfBcoDateOnly : DateTimeFormatter = DateTimeFormat.forPattern("d MMMM").withLocale(Locale("nl"))

}