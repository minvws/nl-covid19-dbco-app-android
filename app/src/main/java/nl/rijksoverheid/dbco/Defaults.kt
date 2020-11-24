/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import kotlinx.serialization.json.Json

/**
 * Created by Dima Kovalenko.
 */
object Defaults {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}