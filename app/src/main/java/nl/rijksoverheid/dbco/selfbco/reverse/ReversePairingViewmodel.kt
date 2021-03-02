/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.onboarding.FillCodeViewModel
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingStatusResponse
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.user.UserRepository
import timber.log.Timber

class ReversePairingViewmodel(val userRepository: IUserRepository) : ViewModel() {

    val reversePairingCode = MutableLiveData<String?>(null)
    var pairingToken: String? = null

    private val _userHasSharedCode = MutableLiveData<Boolean>(false)
    val userHasSharedCode : LiveData<Boolean> = _userHasSharedCode

    private val _shouldBePolling = MutableLiveData<Boolean>(false)
    val shouldBePolling : LiveData<Boolean> = _shouldBePolling

    private val _reversePairingResult = MutableLiveData<ReversePairingStatusResponse>()
    val reversePairingResult : LiveData<ReversePairingStatusResponse> = _reversePairingResult

    private var pollingJob : Job? = null

    fun retrievePairingCode() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val pairingResponse = userRepository.retrieveReversePairingCode()
                Timber.d("Got response $pairingResponse")
                if (pairingResponse.isSuccessful) {
                    pairingResponse.body()?.let { response ->
                        reversePairingCode.postValue(response.code)
                        pairingToken = response.token
                    }
                }
            }
        }
    }

    fun pollForChanges(){
        pairingToken?.let{ token ->
           pollingJob = viewModelScope.launch {
                val poller = ReversePairingStatePoller(userRepository, Dispatchers.IO)
                val flow = poller.poll(10_000, token).onEach { response ->
                    Timber.d("Got response $response")
                    if(response.status == ReversePairingState.COMPLETED && response.pairingCode != null){
                        // User has paired so set both values to true
                        _userHasSharedCode.postValue(true)
                        _reversePairingResult.postValue(response)
                        _shouldBePolling.postValue(false)
                        poller.close()
                        cancelPollingForChanges()
                    }
                }
                flow.collect()

            }
        }
        _shouldBePolling.postValue(true)
    }

    fun setUserHasSharedCode(hasShared : Boolean){
        _userHasSharedCode.postValue(hasShared)
    }

    fun cancelPollingForChanges(){
        pollingJob?.cancel()
        _shouldBePolling.postValue(false)
    }

}
