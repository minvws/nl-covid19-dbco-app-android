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
    val exposureData: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val exposureUI: DateTimeFormatter = DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy").withLocale(Locale("nl"))
}