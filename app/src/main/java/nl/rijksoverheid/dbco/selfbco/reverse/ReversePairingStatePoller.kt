/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState.COMPLETED
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState.PENDING
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.Seconds

class ReversePairingStatePoller(
    private val repository: IUserRepository,
    private val dispatcher: CoroutineDispatcher
) : Poller {

    override fun poll(
        delay: Long,
        credentials: ReversePairingCredentials
    ): Flow<ReversePairingStatus> {
        return channelFlow {
            send(ReversePairingStatus.Pairing(credentials))
            var refreshDelay = delay
            while (!isClosedForSend) {
                try {
                    val response = repository.checkReversePairingStatus(credentials.token)
                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        send(ReversePairingStatus.Error(credentials))
                    } else if (body.status == PENDING) {
                        val refresh = body.refreshDelay!!

                        val now = LocalDateTime.now(DateTimeZone.UTC)
                        val expiry = LocalDateTime.parse(body.expiresAt!!, DateFormats.pairingData)
                        val secondsLeft = Seconds.secondsBetween(now, expiry).seconds
                        if (now.isAfter(expiry) || secondsLeft <= refresh) {
                            send(ReversePairingStatus.Expired)
                        }
                        refreshDelay = refresh * 1_000L
                    } else if (body.status == COMPLETED) {
                        send(ReversePairingStatus.Success(body.pairingCode!!))
                    }
                    delay(refreshDelay)
                } catch (ex: Exception) {
                    if (ex !is CancellationException) {
                        send(ReversePairingStatus.Error(credentials))
                    }
                }
            }
        }.flowOn(dispatcher)
    }

    override fun close() {
        dispatcher.cancel()
        dispatcher.cancelChildren()
    }

    sealed class ReversePairingStatus {
        data class Pairing(val credentials: ReversePairingCredentials) : ReversePairingStatus()
        data class Success(val code: String) : ReversePairingStatus()
        object Expired : ReversePairingStatus()
        object Stopped : ReversePairingStatus()
        data class Error(val credentials: ReversePairingCredentials) : ReversePairingStatus()
    }
}