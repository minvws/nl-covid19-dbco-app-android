/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import java.util.regex.Pattern

/**
 * Created by Dima Kovalenko.
 */
object Constants {

    // Prefs
    const val USER_PREFS = "userPrefs"

    // Pref keys
    const val USER_CHOSE_ADD_CONTACTS_MANUALLY_AFTER_PAIRING_KEY = "userChoseAddContactsManuallyAfterPairing"
    const val USER_SAW_ROOTED_WARNING_KEY = "userSawRootedWarning"

    // Validation patterns
    val PHONE_VALIDATION_MATCHER : Pattern = Pattern.compile("[+]?[0-9]{10,13}$")
}