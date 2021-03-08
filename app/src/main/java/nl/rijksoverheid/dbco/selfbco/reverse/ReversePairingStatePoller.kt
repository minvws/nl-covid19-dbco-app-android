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

    private var errorCount: Int = 0

    override fun poll(delay: Long, token: String): Flow<ReversePairingStatus> {
        return channelFlow {
            send(ReversePairingStatus.Pairing)
            while (!isClosedForSend) {
                var refreshDelay: Long = delay
                try {
                    val response = repository.checkReversePairingStatus(token)
                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        if (errorCount > 2) {
                            send(ReversePairingStatus.Error)
                        } else {
                            errorCount++
                        }
                    } else if (body.status == PENDING) {
                        val now = LocalDateTime.now(DateTimeZone.UTC)
                        val expiry = LocalDateTime.parse(body.expiresAt!!, DateFormats.pairingData)
                        val secondsLeft = Seconds.secondsBetween(now, expiry).seconds
                        val refresh = body.refreshDelay!!
                        if (now.isAfter(expiry) || secondsLeft < refresh) {
                            send(ReversePairingStatus.Expired)
                        }
                        refreshDelay = refresh * 1_000L
                    } else if (body.status == COMPLETED) {
                        send(ReversePairingStatus.Success(body.pairingCode!!))
                    }
                } catch (ex: Exception) {
                    errorCount++
                }
                delay(refreshDelay)
            }
        }.flowOn(dispatcher)
    }

    override fun close() {
        dispatcher.cancel()
        dispatcher.cancelChildren()
    }

    sealed class ReversePairingStatus {
        object Pairing : ReversePairingStatus()
        data class Success(val code: String) : ReversePairingStatus()
        object Expired : ReversePairingStatus()
        object Error : ReversePairingStatus()
    }
}