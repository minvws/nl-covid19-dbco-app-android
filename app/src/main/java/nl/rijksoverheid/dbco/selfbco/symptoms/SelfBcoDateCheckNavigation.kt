/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.symptoms

import java.io.Serializable

sealed class SelfBcoDateCheckNavigation : Serializable {

    object PermissionCheck : SelfBcoDateCheckNavigation()
    object SymptomDateDoubleCheck : SelfBcoDateCheckNavigation()
    object ChronicSymptomCheck : SelfBcoDateCheckNavigation()
    object SymptomsWorsenedCheck : SelfBcoDateCheckNavigation()
}