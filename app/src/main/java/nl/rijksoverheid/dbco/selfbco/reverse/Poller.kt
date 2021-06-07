/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import kotlinx.coroutines.flow.Flow
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus

/**
 * Poller used in the reverse pairing flow
 */
interface Poller {

    /**
     * Start polling for reverse pairing status
     * @param delay polling interval in ms
     * @param credentials given credentials to fetch polling status
     * @return the status
     */
    fun poll(delay: Long, credentials: ReversePairingCredentials): Flow<ReversePairingStatus>

    /**
     * Stop polling
     */
    fun close()
}