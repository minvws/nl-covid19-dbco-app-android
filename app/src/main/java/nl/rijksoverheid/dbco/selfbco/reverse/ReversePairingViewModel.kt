/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus.Success
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus.Pairing
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus.Stopped
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus
import nl.rijksoverheid.dbco.user.IUserRepository

class ReversePairingViewModel(val userRepository: IUserRepository) : ViewModel() {

    private val _pairingCode = MutableLiveData<String>()
    val pairingCode: LiveData<String> = _pairingCode

    private val _userHasSharedCode = MutableLiveData(false)
    val userHasSharedCode: LiveData<Boolean> = _userHasSharedCode

    private val _pairingStatus = MutableLiveData<ReversePairingStatus>()
    val pairingStatus: LiveData<ReversePairingStatus> = _pairingStatus

    private var pollingJob: Job? = null
    private lateinit var poller: Poller

    fun start(credentials: ReversePairingCredentials? = null) {
        if (credentials != null) {
            // need to start pairing with  provided credentials
            _pairingCode.postValue(credentials.code)
            startPairing(credentials)
        } else {
            // need to start with new credentials
            retrievePairingCode()
        }
    }

    private fun retrievePairingCode() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val pairingResponse = userRepository.retrieveReversePairingCode()
                if (pairingResponse.isSuccessful) {
                    val code = pairingResponse.body()?.code
                    val token = pairingResponse.body()?.token
                    if (code != null && token != null) {
                        _pairingCode.postValue(code)
                        startPairing(ReversePairingCredentials(token, code))
                    }
                }
            }
        }
    }

    private fun startPairing(credentials: ReversePairingCredentials) {
        cancelPollingForChanges()
        pollingJob = viewModelScope.launch {
            poller = ReversePairingStatePoller(userRepository, Dispatchers.IO)
            val flow = poller.poll(POLLING_DELAY, credentials).onEach { result ->
                _pairingStatus.postValue(result)
                if (result is Success) {
                    _userHasSharedCode.postValue(true)
                }
                if (result !is Pairing) {
                    poller.close()
                    cancelPollingForChanges()
                }
            }
            flow.collect()
        }
    }

    fun cancelPairing() {
        poller.close()
        cancelPollingForChanges()
        _pairingStatus.postValue(Stopped)
    }

    fun setUserHasSharedCode(hasShared: Boolean) {
        _userHasSharedCode.postValue(hasShared)
    }

    fun cancelPollingForChanges() {
        pollingJob?.cancel()
    }

    companion object {
        private const val POLLING_DELAY = 10_000L
    }
}