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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState
import nl.rijksoverheid.dbco.user.IUserRepository
import timber.log.Timber

class ReversePairingViewmodel(val userRepository: IUserRepository) : ViewModel() {

    val reversePairingCode = MutableLiveData<String?>(null)
    private var pairingToken: String? = null

    private val _userHasSharedCode = MutableLiveData<Boolean>(false)
    private val _userHasPaired = MutableLiveData<Boolean>(false)
    val userHasSharedCode : LiveData<Boolean> = _userHasSharedCode
    val userHasPaired : LiveData<Boolean> = _userHasPaired

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

    fun checkPairingStatus() {
        pairingToken?.let { token ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val statusResponse = userRepository.checkReversePairingStatus(token)
                    Timber.d("Got response $statusResponse")
                    if(statusResponse.isSuccessful){
                        if(statusResponse.body()?.status == ReversePairingState.PENDING){
                            // User has paired so set both values to true
                            _userHasPaired.postValue(true)
                            _userHasSharedCode.postValue(true)
                        }
                    }
                }

            }
        }
    }

    fun pollForChanges(){
        pairingToken?.let{ token ->
            viewModelScope.launch {
                val poller = ReversePairingStatePoller(userRepository, Dispatchers.IO)
                val flow = poller.poll(5_000, token).onEach {
                    Timber.d("Got response $it")
                }.take(2)
                flow.collect()
            }

        }
    }

    fun setUserHasPaired(hasPaired : Boolean){
        _userHasPaired.postValue(hasPaired)
    }

    fun setUserHasSharedCode(hasShared : Boolean){
        _userHasSharedCode.postValue(hasShared)
    }
}
