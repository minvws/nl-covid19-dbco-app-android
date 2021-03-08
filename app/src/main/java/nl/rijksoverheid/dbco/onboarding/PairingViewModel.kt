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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.user.IUserRepository

class PairingViewModel(private val userRepository: IUserRepository) : ViewModel() {

    private val _pairingResult = MutableLiveData<PairingResult>()
    val pairingResult: LiveData<PairingResult> = _pairingResult

    fun pair(pin: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    userRepository.pair(pin)
                    _pairingResult.postValue(PairingResult.Success)
                } catch (ex: Throwable) {
                    _pairingResult.postValue(PairingResult.Error(ex))
                }
            }
        }
    }

    sealed class PairingResult {
        object Success : PairingResult()
        data class Error(val exception: Throwable) : PairingResult()
    }
}