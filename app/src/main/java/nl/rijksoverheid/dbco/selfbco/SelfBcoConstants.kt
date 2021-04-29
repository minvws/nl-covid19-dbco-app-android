/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco

class SelfBcoConstants {
    companion object {
        /**
         * Index has symptoms
         */
        const val SYMPTOM_CHECK_FLOW = 0

        /**
         * User has no symptoms but a positive test result
         */
        const val COVID_CHECK_FLOW = 1
    }
}