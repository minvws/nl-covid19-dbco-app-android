/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.selfbco.reverse.Poller
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingCredentials
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.user.IUserRepository
import retrofit2.HttpException
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus.*
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingStatus.*

@ExperimentalSerializationApi
class PairingViewModel(
    private val userRepository: IUserRepository,
    private val tasksRepository: ITaskRepository
) : ViewModel() {

    private val _pairingResult = MutableLiveData<PairingStatus>()
    val pairingStatus: LiveData<PairingStatus> = _pairingResult

    private val _pairingCode = MutableLiveData<String>()
    val pairingCode: LiveData<String> = _pairingCode

    private val _userHasSharedCode = MutableLiveData(false)
    val userHasSharedCode: LiveData<Boolean> = _userHasSharedCode

    private val _reversePairingStatus = MutableLiveData<ReversePairingStatus>()
    val reversePairingStatus: LiveData<ReversePairingStatus> = _reversePairingStatus

    private var pollingJob: Job? = null
    private lateinit var poller: Poller

    /**
     * Pair with regular pin given by an GGD employee
     */
    fun pair(pin: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _pairingResult.postValue(pairWithCode(pin))
            }
        }
    }

    /**
     * Start reverse pairing flow where the index provides the key to the GGD employee
     * @param credentials optional credentials, leave empty for new credentials
     */
    fun startReversePairing(credentials: ReversePairingCredentials? = null) {
        if (credentials != null) {
            // need to start pairing with provided credentials
            _pairingCode.postValue(credentials.code)
            startPolling(credentials)
        } else {
            // need to start with new credentials
            retrievePairingCode()
        }
    }

    private suspend fun pairWithCode(code: String): PairingStatus {
        return try {
            userRepository.pair(code)
            val case = tasksRepository.fetchCase()
            PairingSuccess(case)
        } catch (ex: Throwable) {
            if (ex is HttpException && ex.code() == 400) {
                PairingInvalid
            } else {
                PairingError(ex)
            }
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
                        startPolling(ReversePairingCredentials(token, code))
                    }
                }
            }
        }
    }

    private fun startPolling(credentials: ReversePairingCredentials) {
        cancelPollingForChanges()
        pollingJob = viewModelScope.launch {
            poller = ReversePairingStatePoller(userRepository, Dispatchers.IO)
            val flow = poller.poll(POLLING_DELAY, credentials).onEach { result ->
                when (result) {
                    is ReversePairingSuccess -> {
                        _userHasSharedCode.postValue(true)
                        when (pairWithCode(result.code)) {
                            is PairingSuccess -> {
                                _reversePairingStatus.postValue(ReversePairingSuccess(result.code))
                            }
                            is PairingInvalid -> {
                                _reversePairingStatus.postValue(ReversePairingExpired)
                            }
                            is PairingError -> {
                                _reversePairingStatus.postValue(ReversePairingError(credentials))
                            }
                        }
                    }
                    else -> _reversePairingStatus.postValue(result)
                }
                if (result !is ReversePairing) {
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
        _reversePairingStatus.postValue(ReversePairingStopped)
    }

    fun setUserHasSharedCode(hasShared: Boolean) {
        _userHasSharedCode.postValue(hasShared)
    }

    fun cancelPollingForChanges() = pollingJob?.cancel()

    companion object {
        private const val POLLING_DELAY = 10_000L
    }

    sealed class PairingStatus {
        data class PairingSuccess(val case: Case) : PairingStatus()
        object PairingInvalid : PairingStatus()
        data class PairingError(val exception: Throwable) : PairingStatus()
    }

    sealed class ReversePairingStatus {
        data class ReversePairing(val credentials: ReversePairingCredentials) : ReversePairingStatus()
        data class ReversePairingSuccess(val code: String) : ReversePairingStatus()
        object ReversePairingExpired : ReversePairingStatus()
        object ReversePairingStopped : ReversePairingStatus()
        data class ReversePairingError(val credentials: ReversePairingCredentials) : ReversePairingStatus()
    }
}