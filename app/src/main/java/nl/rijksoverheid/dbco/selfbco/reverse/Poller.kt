/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import kotlinx.coroutines.flow.Flow
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingResult

interface Poller {
    fun poll(delay: Long, token : String): Flow<ReversePairingResult>
    fun close()
}