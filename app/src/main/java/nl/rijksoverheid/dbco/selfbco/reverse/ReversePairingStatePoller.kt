/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState.COMPLETED
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState.PENDING

class ReversePairingStatePoller(
    private val repository: IUserRepository,
    private val dispatcher: CoroutineDispatcher
) : Poller {
    override fun poll(delay: Long, token: String): Flow<ReversePairingResult> {
        return channelFlow {
            while (!isClosedForSend) {
                val response = repository.checkReversePairingStatus(token)
                val body = response.body()
                var refreshDelay: Long = delay
                if (response.isSuccessful && body != null && body.status == PENDING) {
                    refreshDelay = body.refreshDelay!! * 1_000L
                    // TODO: check expiresAt
                }

                if (response.isSuccessful && body != null && body.status == COMPLETED) {
                    send(ReversePairingResult.Success(body.pairingCode!!))
                }

                delay(refreshDelay)
            }
        }.flowOn(dispatcher)
    }

    override fun close() {
        dispatcher.cancel()
        dispatcher.cancelChildren()
    }

    sealed class ReversePairingResult {
        data class Success(val code: String) : ReversePairingResult()
        object Expired : ReversePairingResult()
    }
}