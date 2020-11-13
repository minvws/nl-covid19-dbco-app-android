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

class FillCodeViewModel(private val userRepository: IUserRepository) : ViewModel() {

    private val _validPairingCode = MutableLiveData<Boolean>()
    val validPairingCode: LiveData<Boolean> = _validPairingCode

    fun pair(pin: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepository.pair(pin)
                if (userRepository.getToken() == null) {
                    _validPairingCode.postValue(false)
                } else {
                    _validPairingCode.postValue(true)
                }
            }
        }
    }
}