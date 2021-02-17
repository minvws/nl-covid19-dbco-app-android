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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingStatusResponse
import nl.rijksoverheid.dbco.user.IUserRepository

class ReversePairingStatePoller(private val repository: IUserRepository, private val dispatcher: CoroutineDispatcher) : Poller {
    override fun poll(delay: Long, token : String): Flow<ReversePairingStatusResponse> {
        return channelFlow {
            while (!isClosedForSend) {
                val data = repository.checkReversePairingStatus(token)
                if(data.isSuccessful) {
                    send(data.body()!!)
                }
                delay(delay)
            }
        }.flowOn(dispatcher)
    }

    override fun close() {
        dispatcher.cancel()
    }
}